/**
 *
 */
package edu.washington.edu.escience.myria.perfenforce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.amazonaws.util.json.JSONException;
import com.google.common.primitives.Ints;

import edu.washington.escience.myria.DbException;
import edu.washington.escience.myria.RelationKey;
import edu.washington.escience.myria.Schema;
import edu.washington.escience.myria.coordinator.CatalogException;
import edu.washington.escience.myria.io.DataSource;
import edu.washington.escience.myria.operator.DbInsert;
import edu.washington.escience.myria.operator.DbQueryScan;
import edu.washington.escience.myria.operator.EOSSource;
import edu.washington.escience.myria.operator.RootOperator;
import edu.washington.escience.myria.operator.SinkRoot;
import edu.washington.escience.myria.operator.network.GenericShuffleConsumer;
import edu.washington.escience.myria.operator.network.GenericShuffleProducer;
import edu.washington.escience.myria.operator.network.partition.IdentityHashPartitionFunction;
import edu.washington.escience.myria.operator.network.partition.RoundRobinPartitionFunction;
import edu.washington.escience.myria.parallel.ExchangePairID;
import edu.washington.escience.myria.parallel.Server;

/**
 * The PerfEnforce Driver
 * 
 */
public class PerfEnforceDriver {

  String configFilePath;
  Server server;
  Set<Integer> configurations;

  // access to an instance of the master server
  public PerfEnforceDriver(final String configFilePath, final Server server) {
    this.configFilePath = configFilePath;
    this.server = server;
    configurations = new HashSet<Integer>(Arrays.asList(4, 6, 8, 10, 12));
  }

  // this should be called from elsewhere
  public void beginSetup() throws JSONException, IOException, InterruptedException, ExecutionException, DbException,
      CatalogException {

    PerfEnforceConfigurationParser configParser = new PerfEnforceConfigurationParser(configFilePath);
    List<TableDescriptionEncoding> factTables = configParser.getTablesOfType("fact");

    // find all fact tables from the parsed config file
    for (int i = 0; i < factTables.size(); i++) {
      TableDescriptionEncoding currentTable = factTables.get(i);
      ingestFact(currentTable.relationkey, currentTable.source, currentTable.schema, currentTable.delimiter,
          configurations);
    }

    // find all dimension tables from the parsed config file
    List<TableDescriptionEncoding> dimensionTables = configParser.getTablesOfType("dimension");
    for (int i = 0; i < dimensionTables.size(); i++) {
      TableDescriptionEncoding currentTable = dimensionTables.get(i);
      ingestDimension(currentTable.relationkey, currentTable.source, currentTable.schema, currentTable.delimiter,
          configurations);
    }

    // run the views and add them to the catalog

    // scan and collect statistics on tables for all columns

    // collect and record all postgres features from one worker
  }

  /*
   * Ingesting the fact table in a parallel sequence
   */
  public void ingestFact(final RelationKey relationKey, final DataSource source, final Schema schema,
      final Character delimiter, final Set<Integer> configurations) {
    ArrayList<Integer> configs = new ArrayList<Integer>(configurations);
    Collections.sort(configs, Collections.reverseOrder());

    // Create a sequence for the largest cluster size
    int maxConfig = configs.get(0);
    Set<Integer> rangeMax = PerfEnforceUtils.getRangeSet(maxConfig);

    // Ingest for the largest cluster size
    RelationKey maxConfigRelationKey =
        new RelationKey(relationKey.getUserName(), relationKey.getProgramName(), relationKey.getRelationName()
            + maxConfig);

    try {
      server.ingestCSVDatasetInParallel(maxConfigRelationKey, source, schema, delimiter, rangeMax);
    } catch (DbException | InterruptedException e1) {
      e1.printStackTrace();
    }

    // Iterate for moving and set parameters
    Set<Integer> previousSequence = rangeMax;
    RelationKey previousRelationKey = maxConfigRelationKey;
    for (int c = 1; c < configs.size(); c++) {
      // get the new worker sequence
      int currentSize = configs.get(c);
      Set<Integer> currentSequence = PerfEnforceUtils.getRangeSet(currentSize);

      // get the worker diff
      Set<Integer> diff = com.google.common.collect.Sets.difference(previousSequence, currentSequence);

      // get the new relation key
      RelationKey currentRelationKey =
          new RelationKey(relationKey.getUserName(), relationKey.getProgramName(), relationKey.getRelationName()
              + currentSize);

      // shuffle the diffs (from previous relation key) to the rest
      final ExchangePairID shuffleId = ExchangePairID.newID();
      DbQueryScan scan = new DbQueryScan(previousRelationKey, schema);

      int[] producingWorkers = PerfEnforceUtils.getRangeInclusive(Collections.min(diff), Collections.max(diff));
      int[] receivingWorkers = PerfEnforceUtils.getRangeInclusive(1, Collections.max(currentSequence));

      GenericShuffleProducer producer =
          new GenericShuffleProducer(scan, shuffleId, receivingWorkers, new RoundRobinPartitionFunction(
              receivingWorkers.length));
      GenericShuffleConsumer consumer = new GenericShuffleConsumer(schema, shuffleId, producingWorkers);
      DbInsert insert = new DbInsert(consumer, currentRelationKey, true);

      Map<Integer, RootOperator[]> workerPlans = new HashMap<>(currentSize);
      for (Integer workerID : producingWorkers) {
        workerPlans.put(workerID, new RootOperator[] { producer });
      }
      for (Integer workerID : receivingWorkers) {
        workerPlans.put(workerID, new RootOperator[] { insert });
      }
      try {
        server.submitQueryPlan(new SinkRoot(new EOSSource()), workerPlans).get();
      } catch (InterruptedException | ExecutionException | DbException | CatalogException e) {
        e.printStackTrace();
      }

      previousSequence = currentSequence;
      previousRelationKey = currentRelationKey;
    }
  }

  /*
   * Ingesting dimension tables for broadcasting
   */
  public void ingestDimension(final RelationKey relationKey, final DataSource source, final Schema schema,
      final Character delimiter, final Set<Integer> configurations) throws InterruptedException, ExecutionException,
      DbException, CatalogException {

    Set<Integer> totalWorkers = PerfEnforceUtils.getRangeSet(Collections.max(configurations));

    try {
      server.ingestCSVDatasetInParallel(relationKey, source, schema, delimiter, totalWorkers);
    } catch (DbException | InterruptedException e1) {
      e1.printStackTrace();
    }

    DbQueryScan dbscan = new DbQueryScan(relationKey, schema);
    final ExchangePairID broadcastID = ExchangePairID.newID();
    GenericShuffleProducer producer =
        new GenericShuffleProducer(dbscan, broadcastID, Ints.toArray(totalWorkers), new IdentityHashPartitionFunction(
            totalWorkers.size()));
    GenericShuffleConsumer consumer = new GenericShuffleConsumer(schema, broadcastID, Ints.toArray(totalWorkers));
    DbInsert insert = new DbInsert(consumer, relationKey, true);
    Map<Integer, RootOperator[]> workerPlans = new HashMap<>(totalWorkers.size());
    for (Integer workerID : totalWorkers) {
      workerPlans.put(workerID, new RootOperator[] { producer, insert });
    }
    server.submitQueryPlan(new SinkRoot(new EOSSource()), workerPlans).get();
  }

  public void createPostgresViews() {

  }

  public void collectPostgresFeatures() {
    // run something on postgres and output results to some directly -- possibly the same as the configuration ---
    // this should first scan all
  }

  public void generatePSLA() {
    // calls the C# program via mono and gets the PSLA
  }

  public void beginQueryMonitoring() {

  }

}
