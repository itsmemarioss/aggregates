package br.com.itsmemario.ddd.aggregates;

import java.math.BigDecimal;

public record Product (String code, BigDecimal price){}
