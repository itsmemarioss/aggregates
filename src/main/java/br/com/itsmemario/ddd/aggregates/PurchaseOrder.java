package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrder {
  private final BigDecimal approvedLimit;
  /*
  items can be duplicated, I considered a set, but I kept it as simple as possible
  */
  private final List<PurchaseOrderLineItem> items = new ArrayList<>();

  public PurchaseOrder(BigDecimal approvedLimit) {
    this.approvedLimit = approvedLimit;
  }

  public int addItem(Product product, int quantity) throws ApprovedLimitException {
    var lineItem = new PurchaseOrderLineItem(product, quantity);
    if (itemExceedsTheLimit(lineItem)) {
      throw new ApprovedLimitException("Item exceeds the approved limit");
    }
    items.add(lineItem);
    /*
    item id is implicit, I think I should have created an id attribute in the lineItem
    but since I'd have to control this after adding/removing I decided use the list index
     */
    return items.size();
  }

  private boolean itemExceedsTheLimit(PurchaseOrderLineItem lineItem) {
    return approvedLimitIsLessThan(lineItem.total()) | limitIsNotEnough(lineItem);
  }

  private boolean limitIsNotEnough(PurchaseOrderLineItem lineItem) {
    Optional<BigDecimal> itemsTotal = calculateItemsTotal();
    if (itemsTotal.isPresent()) {
      return approvedLimit.compareTo(lineItem.total().add(itemsTotal.get())) < 0;
    }
    return false;
  }

  private boolean approvedLimitIsLessThan(BigDecimal itemTotal) {
    return approvedLimit.compareTo(itemTotal) < 0;
  }

  private Optional<BigDecimal> calculateItemsTotal() {
    return items.stream().map(PurchaseOrderLineItem::total).reduce(BigDecimal::add);
  }

  public void changeQuantity(int id, int quantity) throws ApprovedLimitException {
    if (quantity <= 0) {
      throw new IllegalArgumentException("New quantity cannot be less than or equal to zero");
    }
    var item = getItemById(id);
    var newQuantity = quantity - item.getQuantity();
    if (limitIsNotEnough(new PurchaseOrderLineItem(item.getProduct(), newQuantity, item.getPrice()))) {
      throw new ApprovedLimitException("New quantity specified exceed the approved limit value");
    }
    item.setQuantity(quantity);
  }

  private PurchaseOrderLineItem getItemById(int id) {
    return items.get(id - 1);
  }

  public BigDecimal total() {
    return calculateItemsTotal().orElse(BigDecimal.ZERO);
  }

  public void changePrice(int id, BigDecimal newPrice) throws ApprovedLimitException {
    if(newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0 ) {
      throw new IllegalArgumentException("Illegal value for newPrice");
    }
    var item = getItemById(id);
    PurchaseOrderLineItem itemWithNewPrice = new PurchaseOrderLineItem(item.getProduct(), item.getQuantity(), newPrice);
    var newTotal = total().subtract(item.total()).add(itemWithNewPrice.total());

    if (approvedLimitIsLessThan(newTotal)) {
      throw new ApprovedLimitException("New quantity specified exceed the approved limit value");
    } else {
      item.setPrice(newPrice);
    }
  }

  public void removeItem(int id) {
    items.remove(id-1);
  }
}
