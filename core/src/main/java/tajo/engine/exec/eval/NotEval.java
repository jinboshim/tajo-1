/**
 * 
 */
package tajo.engine.exec.eval;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import tajo.catalog.Schema;
import tajo.catalog.SchemaUtil;
import tajo.catalog.proto.CatalogProtos.DataType;
import tajo.datum.Datum;
import tajo.datum.DatumFactory;
import tajo.storage.Tuple;

/**
 * @author Hyunsik Choi
 */
public class NotEval extends EvalNode implements Cloneable {
  @Expose private EvalNode subExpr;
  private static final DataType [] RES_TYPE = SchemaUtil
      .newNoNameSchema(DataType.BOOLEAN);

  public NotEval(EvalNode subExpr) {
    super(Type.NOT);
    Preconditions.checkArgument(
        subExpr instanceof BinaryEval || subExpr instanceof NotEval);
    this.subExpr = subExpr;
  }

  @Override
  public EvalContext newContext() {
    NotEvalCtx newCtx = new NotEvalCtx();
    newCtx.subExprCtx = subExpr.newContext();
    return newCtx;
  }

  @Override
  public DataType [] getValueType() {
    return RES_TYPE;
  }

  @Override
  public String getName() {
    return "?";
  }

  @Override
  public void eval(EvalContext ctx, Schema schema, Tuple tuple) {
    subExpr.eval(((NotEvalCtx)ctx).subExprCtx, schema, tuple);
  }

  @Override
  public Datum terminate(EvalContext ctx) {
    return DatumFactory.createBool(!subExpr.terminate(((NotEvalCtx)ctx).subExprCtx).asBool());
  }

  @Override
  public String toString() {
    return "NOT " + subExpr.toString();
  }

  @Override
  public void preOrder(EvalNodeVisitor visitor) {
    visitor.visit(this);
    if (subExpr instanceof NotEval) {
      ((NotEval)subExpr).subExpr.preOrder(visitor);
    } else {
      subExpr.leftExpr.preOrder(visitor);
      subExpr.rightExpr.preOrder(visitor);
    }
  }

  @Override
  public void postOrder(EvalNodeVisitor visitor) {    
    if (subExpr instanceof NotEval) {
      ((NotEval)subExpr).subExpr.preOrder(visitor);
    } else {
      subExpr.leftExpr.preOrder(visitor);
      subExpr.rightExpr.preOrder(visitor);
    }
    visitor.visit(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NotEval) {
      NotEval other = (NotEval) obj;
      return this.subExpr.equals(other.subExpr);
    } else {
      return false;
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    NotEval eval = (NotEval) super.clone();
    eval.subExpr = (EvalNode) this.subExpr.clone();
    return eval;
  }

  private class NotEvalCtx implements EvalContext {
    EvalContext subExprCtx;
  }
}