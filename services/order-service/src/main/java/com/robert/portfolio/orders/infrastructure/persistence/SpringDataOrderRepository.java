package com.robert.portfolio.orders.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    // Spring Data derives this query from the method name and uses the database index/unique key.
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);

    // Intentional fetch join: it loads the collection in the same SQL query and avoids N+1.
    // DISTINCT removes duplicate Order roots caused by the one-to-many join.
    @Query("select distinct orderEntity from OrderJpaEntity orderEntity left join fetch orderEntity.lines")
    List<OrderJpaEntity> findAllWithLinesUsingFetchJoin();

    // EntityGraph keeps the JPQL simple while declaring which association should be fetched eagerly
    // for this query only. It avoids changing the mapping to EAGER globally.
    @EntityGraph(attributePaths = "lines")
    @Query("select orderEntity from OrderJpaEntity orderEntity")
    List<OrderJpaEntity> findAllWithLinesUsingEntityGraph();

    // Constructor projection selects only the data needed by the read use case.
    // No OrderJpaEntity or lazy collection is materialized for this path.
    @Query("""
            select new com.robert.portfolio.orders.infrastructure.persistence.OrderQueryProjection(
                orderEntity.id, orderEntity.customerId, count(line))
            from OrderJpaEntity orderEntity
            left join orderEntity.lines line
            group by orderEntity.id, orderEntity.customerId
            """)
    List<OrderQueryProjection> findOrderSummariesUsingProjection();
}
