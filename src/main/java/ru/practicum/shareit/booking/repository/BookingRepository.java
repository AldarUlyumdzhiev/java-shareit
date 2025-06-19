package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                           Long itemId,
                                                           Status status,
                                                           LocalDateTime before);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId AND b.status = :status AND b.start < :now
            ORDER BY b.end DESC
            """)
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId,
                                      @Param("status") Status status,
                                      @Param("now") LocalDateTime now);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId AND b.status = :status AND b.start > :now
            ORDER BY b.start ASC
            """)
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId,
                                      @Param("status") Status status,
                                      @Param("now") LocalDateTime now);

    List<Booking> findByItemIdIn(List<Long> itemIds);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByBookerId(@Param("bookerId") Long bookerId);
}
