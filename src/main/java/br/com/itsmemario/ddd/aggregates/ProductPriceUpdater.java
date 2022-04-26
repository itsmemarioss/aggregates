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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductPriceUpdater {
  private ProductRepository productRepository;
  private PurchaseOrderRepository purchaseOrderRepository;

  public ProductPriceUpdater(ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository) {
    this.productRepository = productRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  public void updatePrice(String productCode, BigDecimal newPrice) throws ApprovedLimitException {
    Optional<Product> productOptional = productRepository.findByCode(productCode);
    if (productOptional.isPresent()) {
      updateOpenOrdersWithHigherPrices(productCode, newPrice);
      productRepository.save(new Product(productOptional.get().code(), newPrice));
    }
  }

  private void updateOpenOrdersWithHigherPrices(String productCode, BigDecimal newPrice) throws ApprovedLimitException {
    List<PurchaseOrder> openOrdersWithProduct = purchaseOrderRepository.findOpenOrdersWithProduct(productCode);
    for (var order : openOrdersWithProduct) {
      var item = order.getItemByCode(productCode);
      if (item.getPrice().compareTo(newPrice) > 0) {
        //todo change exception to another more specific
        //the exception should never happen since the value is being decreased
        order.changePrice(productCode,newPrice);
        purchaseOrderRepository.save(order);
      }
    }
  }

}
