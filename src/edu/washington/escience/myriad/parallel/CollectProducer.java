package edu.washington.escience.myriad.parallel;

import java.util.Arrays;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import edu.washington.escience.myriad.DbException;
import edu.washington.escience.myriad.Schema;
import edu.washington.escience.myriad.TupleBatch;
import edu.washington.escience.myriad.TupleBatchBuffer;
import edu.washington.escience.myriad.column.Column;
import edu.washington.escience.myriad.operator.Operator;
import edu.washington.escience.myriad.proto.DataProto.ColumnMessage;
import edu.washington.escience.myriad.proto.DataProto.DataMessage;
import edu.washington.escience.myriad.proto.DataProto.DataMessage.DataMessageType;
import edu.washington.escience.myriad.proto.TransportProto.TransportMessage;
import edu.washington.escience.myriad.proto.TransportProto.TransportMessage.TransportMessageType;
import edu.washington.escience.myriad.table._TupleBatch;

/**
 * The producer part of the Collect Exchange operator.
 * 
 * The producer actively pushes the tuples generated by the child operator to the paired CollectConsumer.
 * 
 */
public final class CollectProducer extends Producer {

  /**
   * The working thread, which executes the child operator and send the tuples to the paired CollectConsumer operator.
   */
  class WorkingThread extends Thread {
    @Override
    public void run() {

      final IoSession session = getThisWorker().connectionPool.get(collectConsumerWorkerID, null, 3, null);

      final TransportMessage.Builder messageBuilder = TransportMessage.newBuilder();

      try {

        TupleBatchBuffer buffer = new TupleBatchBuffer(getSchema());

        _TupleBatch tup = null;
        while ((tup = child.next()) != null) {

          final List<Column<?>> fromColumns = tup.outputRawData();

          int numTuples = tup.numOutputTuples();
          int numColumns = fromColumns.size();
          for (int i = 0; i < numTuples; i++) {
            for (int j = 0; j < numColumns; j++) {
              buffer.put(j, fromColumns.get(j).get(i));
            }
          }

          while ((tup = buffer.popFilled()) != null) {
            final List<Column<?>> columns = tup.outputRawData();
            final ColumnMessage[] columnProtos = new ColumnMessage[columns.size()];
            int i = 0;
            for (final Column<?> c : columns) {
              columnProtos[i] = c.serializeToProto();
              i++;
            }
            session.write(messageBuilder.setType(TransportMessageType.DATA).setData(
                DataMessage.newBuilder().setType(DataMessageType.NORMAL).addAllColumns(Arrays.asList(columnProtos))
                    .setOperatorID(CollectProducer.this.operatorID.getLong()).build()).build());
          }
        }

        if (buffer.numTuples() > 0) {
          List<TupleBatch> remain = buffer.getAll();
          for (TupleBatch tb : remain) {
            final List<Column<?>> columns = tb.outputRawData();
            final ColumnMessage[] columnProtos = new ColumnMessage[columns.size()];
            int j = 0;
            for (final Column<?> c : columns) {
              columnProtos[j] = c.serializeToProto();
              j++;
            }

            session.write(messageBuilder.setType(TransportMessageType.DATA).setData(
                DataMessage.newBuilder().setType(DataMessageType.NORMAL).addAllColumns(Arrays.asList(columnProtos))
                    .setOperatorID(CollectProducer.this.operatorID.getLong()).build()).build());
          }
        }

        final DataMessage eos =
            DataMessage.newBuilder().setType(DataMessageType.EOS).setOperatorID(
                CollectProducer.this.operatorID.getLong()).build();
        session.write(messageBuilder.setType(TransportMessageType.DATA).setData(eos).build());

      } catch (final DbException e) {
        e.printStackTrace();
      }
    }
  }

  /** Required for Java serialization. */
  private static final long serialVersionUID = 1L;

  private transient WorkingThread runningThread;
  public static final int MAX_SIZE = 100;
  public static final int MIN_SIZE = 100;

  public static final int MAX_MS = 1000;
  /**
   * The paired collect consumer address.
   */
  private final int collectConsumerWorkerID;

  private Operator child;

  public CollectProducer(final Operator child, final ExchangePairID operatorID, final int collectConsumerWorkerID) {
    super(operatorID);
    this.child = child;
    this.collectConsumerWorkerID = collectConsumerWorkerID;
  }

  @Override
  public void cleanup() {
  }

  @Override
  protected _TupleBatch fetchNext() throws DbException {
    try {
      // wait until the working thread terminate and return an empty tuple set
      runningThread.join();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Operator[] getChildren() {
    return new Operator[] { child };
  }

  @Override
  public Schema getSchema() {
    return child.getSchema();
  }

  @Override
  public void init() throws DbException {
    runningThread = new WorkingThread();
    runningThread.start();
  }

  @Override
  public void setChildren(final Operator[] children) {
    child = children[0];
  }

  @Override
  public _TupleBatch fetchNextReady() throws DbException {
    return fetchNext();
  }

}
