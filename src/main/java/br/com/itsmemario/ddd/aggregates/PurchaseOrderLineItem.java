package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;

public class PurchaseOrderLineItem {
  private int quantity;
  private BigDecimal price;

  public PurchaseOrderLineItem(int quantity, BigDecimal price) {
    this.quantity = quantity;
    this.price = price;
  }

  public int getQuantity() {
    return quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  /*
  set methods were made package private to avoid being used outside the package
  should this object be immutable? and should I recreate a new instance of the line item?
   */
  void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  void setPrice(BigDecimal price) {
    this.price = price;
  }

  public BigDecimal total() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }

}

