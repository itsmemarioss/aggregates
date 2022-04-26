/*
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2022 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
package br.com.itsmemario.ddd.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPriceUpdaterTest {

  private PurchaseOrderRepository purchaseOrderRepository;
  private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository = new ProductRepository();
    purchaseOrderRepository = new PurchaseOrderRepository();
  }

  @Test
  void updatePriceTest() throws ApprovedLimitException {
    String productCode = "code";
    productRepository.save(new Product(productCode,BigDecimal.ZERO));

    ProductPriceUpdater priceUpdater = new ProductPriceUpdater(productRepository, purchaseOrderRepository);
    priceUpdater.updatePrice(productCode, BigDecimal.TEN);

    assertThat(productRepository.findByCode(productCode))
        .isNotEmpty()
        .contains(new Product(productCode, BigDecimal.TEN));
  }

  @Test
  void updateOpenPurchaseOrderWithHigherPrices() throws ApprovedLimitException {
    Product product = new Product("code", BigDecimal.TEN);
    productRepository.save(product);

    var purchaseOrder = new PurchaseOrder(BigDecimal.TEN);
    purchaseOrder.addItem(product, 1);
    var id = purchaseOrderRepository.save(purchaseOrder);
    BigDecimal total = purchaseOrder.total();

    ProductPriceUpdater priceUpdater = new ProductPriceUpdater(productRepository, purchaseOrderRepository);
    priceUpdater.updatePrice(product.code(), BigDecimal.ONE);

    PurchaseOrder savedPurchase = purchaseOrderRepository.getById(id).get();
    BigDecimal newTotal = savedPurchase.total();

    assertThat(newTotal).isLessThan(total);
    assertThat(savedPurchase.geItems()).isNotEmpty();
  }

  @Test
  void doesNotUpdateOpenPurchaseOrderIfPriceGoesUp() throws ApprovedLimitException {
    Product product = new Product("code", BigDecimal.ONE);
    productRepository.save(product);

    var purchaseOrder = new PurchaseOrder(BigDecimal.TEN);
    purchaseOrder.addItem(product, 10);
    var id = purchaseOrderRepository.save(purchaseOrder);
    BigDecimal total = purchaseOrder.total();

    ProductPriceUpdater priceUpdater = new ProductPriceUpdater(productRepository, purchaseOrderRepository);
    priceUpdater.updatePrice(product.code(), BigDecimal.TEN);

    PurchaseOrder savedPurchase = purchaseOrderRepository.getById(id).get();
    BigDecimal newTotal = savedPurchase.total();

    assertThat(newTotal).isEqualTo(total);
    assertThat(savedPurchase.geItems()).isNotEmpty();
  }
}