package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;
import java.util.*;

public class PurchaseOrder {
  private final BigDecimal approvedLimit;
  private boolean open = true;
  /*
  items can be duplicated, I considered a set, but I kept it as simple as possible
  */
  private final Map<String,PurchaseOrderLineItem> items = new HashMap<>();

  public PurchaseOrder(BigDecimal approvedLimit) {
    this.approvedLimit = approvedLimit;
  }

  public String addItem(Product product, int quantity) throws ApprovedLimitException {
    var lineItem = new PurchaseOrderLineItem(product, quantity);
    if (itemExceedsTheLimit(lineItem)) {
      throw new ApprovedLimitException("Item exceeds the approved limit");
    }
    items.put(product.code(), lineItem);
    return product.code();
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
    return items.values().stream().map(PurchaseOrderLineItem::total).reduce(BigDecimal::add);
  }

  public void changeQuantity(String productCode, int quantity) throws ApprovedLimitException {
    if (quantity <= 0) {
      throw new IllegalArgumentException("New quantity cannot be less than or equal to zero");
    }
    var item = getItemByCode(productCode);
    var newQuantity = quantity - item.getQuantity();
    if (limitIsNotEnough(new PurchaseOrderLineItem(item.getProduct(), newQuantity, item.getPrice()))) {
      throw new ApprovedLimitException("New quantity specified exceed the approved limit value");
    }
    item.setQuantity(quantity);
  }

  public PurchaseOrderLineItem getItemByCode(String id) {
    return items.get(id);
  }

  public BigDecimal total() {
    return calculateItemsTotal().orElse(BigDecimal.ZERO);
  }

  public void changePrice(String id, BigDecimal newPrice) throws ApprovedLimitException {
    if(newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0 ) {
      throw new IllegalArgumentException("Illegal value for newPrice");
    }
    var item = getItemByCode(id);
    PurchaseOrderLineItem itemWithNewPrice = new PurchaseOrderLineItem(item.getProduct(), item.getQuantity(), newPrice);
    var newTotal = total().subtract(item.total()).add(itemWithNewPrice.total());

    if (approvedLimitIsLessThan(newTotal)) {
      throw new ApprovedLimitException("New quantity specified exceed the approved limit value");
    } else {
      item.setPrice(newPrice);
    }
  }

  public void removeItem(String productCode) {
    items.remove(productCode);
  }

  public boolean isOpen() {
    return open;
  }

  public boolean containsProduct(String productCode) {
    return items.containsKey(productCode);
  }

  public Collection<PurchaseOrderLineItem> geItems() {
    return Collections.unmodifiableCollection(items.values());
  }
}
