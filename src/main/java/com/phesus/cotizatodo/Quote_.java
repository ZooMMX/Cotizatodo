package com.phesus.cotizatodo;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 22/02/15
 * Time: 19:41
 */
@javax.persistence.metamodel.StaticMetamodel(Quote.class)
public class Quote_ {
    public static volatile SingularAttribute<Quote,Long> id;
    public static volatile SingularAttribute<Quote,Long> targetName;
    public static volatile SingularAttribute<Quote, String> username;
}
