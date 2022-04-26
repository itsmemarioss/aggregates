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
  @CsvSource(value = {"A,100,2,100", "B,50,1,51"})
  void addLineItemThrowsExceptionIfLimitIsReached(String productCode, int limit, int quantity, int price) {
    var product = new Product(productCode,BigDecimal.valueOf(price));
    PurchaseOrder purchaseOrder = new PurchaseOrder(BigDecimal.valueOf(limit));
    assertThatThrownBy(() -> purchaseOrder.addItem(product, quantity)).isInstanceOf(ApprovedLimitException.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"A,200,2,100", "B,60,1,51"})
  void addLineItemSucceedIfItFitsInTheLimit(String productCode, int limit, int quantity, int price) {
    var product = new Product(productCode,BigDecimal.valueOf(price));
    PurchaseOrder purchaseOrder = new PurchaseOrder(BigDecimal.valueOf(limit));
    Assertions.assertThatCode(() -> purchaseOrder.addItem(product, quantity))
            .doesNotThrowAnyException();
  }

  @Test
  void whenAddingAnItemPoShouldCheckTheCurrentTotal() {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    Assertions.assertThatCode(
        () -> purchaseOrder.addItem(product, 1)
    ).doesNotThrowAnyException();

    assertThatThrownBy(
        () -> purchaseOrder.addItem(product, 10)
    ).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void ifIncreasingTheQuantityBreaksTheApprovedLimitThrowsAnException() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 1);
    assertThatThrownBy(() -> purchaseOrder.changeQuantity(id, 20)).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void updateQuantityTest() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 1);
    purchaseOrder.changeQuantity(id, 2);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(20));
  }

  @Test
  void ifQuantityIsReducedRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 10);
    purchaseOrder.changeQuantity(id, 9);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(90));
  }

  @Test
  void ifIncreasingThePriceBreaksTheApprovedLimitThrowsAnException() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 1);

    assertThatThrownBy(
        () -> purchaseOrder.changePrice(id, BigDecimal.valueOf(200))
    ).isInstanceOf(ApprovedLimitException.class);
  }

  @Test
  void ifPriceChangesRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 10);
    purchaseOrder.changePrice(id, BigDecimal.ONE);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.valueOf(10));
  }

  @Test
  void ifALineItemGetsRemovedRecalculateTheTotal() throws ApprovedLimitException {
    BigDecimal limit = BigDecimal.valueOf(100);
    PurchaseOrder purchaseOrder = new PurchaseOrder(limit);
    var product = new Product("productCode",BigDecimal.TEN);
    int id = purchaseOrder.addItem(product, 10);
    purchaseOrder.removeItem(id);
    assertThat(purchaseOrder.total()).isEqualTo(BigDecimal.ZERO);
  }
}