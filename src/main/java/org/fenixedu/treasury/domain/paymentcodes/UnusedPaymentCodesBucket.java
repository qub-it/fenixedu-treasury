package org.fenixedu.treasury.domain.paymentcodes;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.FenixFramework;

public class UnusedPaymentCodesBucket extends UnusedPaymentCodesBucket_Base {
    
    public static final Comparator<UnusedPaymentCodesBucket> COMPARE_BY_BUCKET_INDEX = (o1, o2) -> {
        int c = Integer.compare(o1.getBucketIndex(), o2.getBucketIndex());
        
        if(c != 0) {
            return c;
        }
        
        return o1.getExternalId().compareTo(o2.getExternalId());
    };
    
    public UnusedPaymentCodesBucket() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    public UnusedPaymentCodesBucket(int bucketIndex) {
        this();
        setBucketIndex(bucketIndex);
        
        checkRules();
    }

    private void checkRules() {
        
        if(getDomainRoot() == null) {
            throw new TreasuryDomainException("error.UnusedPaymentCodesBucket.domainRoot.required");
        }
        
        if(findByBucketIndex(getBucketIndex()).count() > 1) {
            throw new TreasuryDomainException("error.UnusedPaymentCodesBucket.bucketIndex.duplicated");
        }
        
    }

    public static Stream<UnusedPaymentCodesBucket> findAll() {
        return FenixFramework.getDomainRoot().getUnusedPaymentCodesBucketsSet().stream();
    }

    public static Stream<UnusedPaymentCodesBucket> findByBucketIndex(int bucketIndex) {
        return findAll().filter(b -> b.getBucketIndex() == bucketIndex);
    }

    public static UnusedPaymentCodesBucket create(final int bucketIndex) {
        return new UnusedPaymentCodesBucket(bucketIndex);
    }
    
}
