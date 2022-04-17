package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PurchaseOrderLineItemTest {

  @Test
  void total() {
    PurchaseOrderLineItem item = new PurchaseOrderLineItem(2, BigDecimal.TEN);
    assertThat(item.total()).isEqualTo(BigDecimal.valueOf(20));
  }
}