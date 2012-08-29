package edu.washington.escience.myriad;

import java.io.Serializable;

import edu.washington.escience.myriad.column.BooleanColumn;
import edu.washington.escience.myriad.column.Column;
import edu.washington.escience.myriad.column.DoubleColumn;
import edu.washington.escience.myriad.column.FloatColumn;
import edu.washington.escience.myriad.column.IntColumn;
import edu.washington.escience.myriad.column.LongColumn;
import edu.washington.escience.myriad.column.StringColumn;

/**
 * Class representing a type in SimpleDB. Types are static objects defined by this class; hence, the
 * Type constructor is private.
 */
public enum Type implements Serializable {
  INT_TYPE() {
    public boolean compare(final Predicate.Op op, final int valueInTuple, final int operand) {
      switch (op) {
        case EQUALS:
          return valueInTuple == operand;
        case NOT_EQUALS:
          return valueInTuple != operand;

        case GREATER_THAN:
          return valueInTuple > operand;

        case GREATER_THAN_OR_EQ:
          return valueInTuple >= operand;

        case LESS_THAN:
          return valueInTuple < operand;

        case LESS_THAN_OR_EQ:
          return valueInTuple <= operand;

        case LIKE:
          return valueInTuple == operand;
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column intColumn, final int tupleIndex,
        final Object operand) {
      int v = ((IntColumn) intColumn).getInt(tupleIndex);
      return this.compare(op, v, (Integer) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return "" + ((IntColumn) column).getInt(tupleIndex);
    }

  },
  FLOAT_TYPE() {
    public boolean compare(final Predicate.Op op, final float valueInTuple, final float operand) {
      switch (op) {
        case EQUALS:
          return valueInTuple == operand;
        case NOT_EQUALS:
          return valueInTuple != operand;

        case GREATER_THAN:
          return valueInTuple > operand;

        case GREATER_THAN_OR_EQ:
          return valueInTuple >= operand;

        case LESS_THAN:
          return valueInTuple < operand;

        case LESS_THAN_OR_EQ:
          return valueInTuple <= operand;

        case LIKE:
          return valueInTuple == operand;
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column floatColumn, final int tupleIndex,
        final Object operand) {
      float v = ((FloatColumn) floatColumn).getFloat(tupleIndex);
      return this.compare(op, v, (Float) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return "" + ((FloatColumn) column).getFloat(tupleIndex);
    }

  },
  DOUBLE_TYPE() {
    public boolean compare(final Predicate.Op op, final double valueInTuple, final double operand) {
      switch (op) {
        case EQUALS:
          return valueInTuple == operand;
        case NOT_EQUALS:
          return valueInTuple != operand;

        case GREATER_THAN:
          return valueInTuple > operand;

        case GREATER_THAN_OR_EQ:
          return valueInTuple >= operand;

        case LESS_THAN:
          return valueInTuple < operand;

        case LESS_THAN_OR_EQ:
          return valueInTuple <= operand;

        case LIKE:
          return valueInTuple == operand;
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column doubleColumn, final int tupleIndex,
        final Object operand) {
      double v = ((DoubleColumn) doubleColumn).getDouble(tupleIndex);
      return this.compare(op, v, (Double) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return "" + ((DoubleColumn) column).getDouble(tupleIndex);
    }

  },
  BOOLEAN_TYPE() {
    public boolean compare(final Predicate.Op op, final boolean valueInTuple, final boolean operand) {
      switch (op) {
        case EQUALS:
          return valueInTuple == operand;
        case NOT_EQUALS:
          return valueInTuple != operand;
        case LIKE:
          return valueInTuple == operand;

        case GREATER_THAN:
        case GREATER_THAN_OR_EQ:
        case LESS_THAN:
        case LESS_THAN_OR_EQ:
          throw new UnsupportedOperationException("BOOLEAN_TYPE not == or != or LIKE");
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column booleanColumn, final int tupleIndex,
        final Object operand) {
      boolean v = ((BooleanColumn) booleanColumn).getBoolean(tupleIndex);
      return this.compare(op, v, (Boolean) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return ((BooleanColumn) column).getBoolean(tupleIndex) + "";
    }
  },
  STRING_TYPE() {
    public boolean compare(final Predicate.Op op, final String valInTuple, final String operand) {

      int cmpVal = valInTuple.compareTo(operand);

      switch (op) {
        case EQUALS:
          return cmpVal == 0;

        case NOT_EQUALS:
          return cmpVal != 0;

        case GREATER_THAN:
          return cmpVal > 0;

        case GREATER_THAN_OR_EQ:
          return cmpVal >= 0;

        case LESS_THAN:
          return cmpVal < 0;

        case LESS_THAN_OR_EQ:
          return cmpVal <= 0;

        case LIKE:
          return valInTuple.indexOf(operand) >= 0;
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column stringColumn, final int tupleIndex,
        final Object operand) {
      String string = ((StringColumn) stringColumn).getString(tupleIndex);
      return this.compare(op, string, (String) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return ((StringColumn) column).getString(tupleIndex);
    }
  },
  LONG_TYPE() {
    public boolean compare(final Predicate.Op op, final long valueInTuple, final long operand) {
      switch (op) {
        case EQUALS:
          return valueInTuple == operand;
        case NOT_EQUALS:
          return valueInTuple != operand;

        case GREATER_THAN:
          return valueInTuple > operand;

        case GREATER_THAN_OR_EQ:
          return valueInTuple >= operand;

        case LESS_THAN:
          return valueInTuple < operand;

        case LESS_THAN_OR_EQ:
          return valueInTuple <= operand;

        case LIKE:
          return valueInTuple == operand;
      }

      return false;
    }

    @Override
    public boolean filter(final Predicate.Op op, final Column longColumn, final int tupleIndex,
        final Object operand) {
      long v = ((LongColumn) longColumn).getLong(tupleIndex);
      return this.compare(op, v, (Long) operand);
    }

    @Override
    public String toString(final Column column, final int tupleIndex) {
      return "" + ((LongColumn) column).getLong(tupleIndex);
    }

  };

  public abstract boolean filter(Predicate.Op op, Column column, int tupleIndex, Object operand);

  public abstract String toString(Column column, int tupleIndex);
}