package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderTest {

  @ParameterizedTest
  @CsvSource(value = {"100,2,100", "50,1,51"})
  void addLineItemThrowsExceptionIfLimitIsReached(int limit, int quantity, int value) {
    PurchaseOrder purchaseOrder = new PurchaseOrder(BigDecimal.valueOf(limit));
    PurchaseOrderLineItem lineItem = new PurchaseOrderLineItem(quantity, BigDecimal.valueOf(value));
    assertThatThrownBy(() -> purchaseOrder.addItem(lineItem)).isInstanceOf(ApprovedLimitException.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"200,2,100", "60,1,51"})
  void addLineItemSucceedIfItFitsInTheLimit(int limit, int quantity, int value) {
    PurchaseOrder purchaseOrder = new PurchaseOrder(BigDecimal.valueOf(limit));
    PurchaseOrderLineItem lineItem = new PurchaseOrderLineItem(quantity, BigDecimal.valueOf(value));
    Assertions.assertThatCode(() -> purchaseOrder.addItem(lineItem)).doesNotThrowAnyException();
  }

  @Test
  void whenAddingAnItemPoShouldCheckTheCurrentTotal() {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);

    Assertions.assertThatCode(
        () -> purchaseOrder.addItem(new PurchaseOrderLineItem(1, BigDecimal.TEN))
    ).doesNotThrowAnyException();

    assertThatThrownBy(
        () -> purchaseOrder.addItem(new PurchaseOrderLineItem(10, BigDecimal.TEN))
    ).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void ifIncreasingTheQuantityBreaksTheApprovedLimitThrowsAnException() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(1, BigDecimal.TEN));
    assertThatThrownBy(() -> purchaseOrder.changeQuantity(id, 20)).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void updateQuantityTest() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(1, BigDecimal.TEN));
    purchaseOrder.changeQuantity(id, 2);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(20));
  }

  @Test
  void ifQuantityIsReducedRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(10, BigDecimal.TEN));
    purchaseOrder.changeQuantity(id, 9);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(90));
  }

  @Test
  void ifIncreasingThePriceBreaksTheApprovedLimitThrowsAnException() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(1, BigDecimal.TEN));

    assertThatThrownBy(
        () -> purchaseOrder.changePrice(id, BigDecimal.valueOf(200))
    ).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void ifPriceChangesRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(10, BigDecimal.TEN));
    purchaseOrder.changePrice(id, BigDecimal.ONE);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(10));
  }

  @Test
  void ifALineItemGetsRemovedRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    int id = purchaseOrder.addItem(new PurchaseOrderLineItem(10, BigDecimal.TEN));
    purchaseOrder.removeItem(id);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.ZERO);
  }
}