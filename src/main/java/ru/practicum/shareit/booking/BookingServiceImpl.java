package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOut saveNewBooking(BookingDtoIn bookingDtoIn, long userId) {
        User booker = getUser(userId);
        Item item = getItem(bookingDtoIn.getItemId());
        if (!item.getAvailable()) {
            throw new ItemIsNotAvailableException("Вещь недоступна для брони");
        }
        if (booker.getId() == item.getOwner().getId()) {
            throw new NotAvailableToBookOwnItemsException("Функция бронировать собственную вещь отсутствует");
        }
        if (!bookingDtoIn.getEnd().isAfter(bookingDtoIn.getStart()) ||
                bookingDtoIn.getStart().isBefore(LocalDateTime.now())) {
            throw new WrongDatesException("Дата начала бронирования должна быть раньше даты возврата");
        }
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        bookingRepository.save(BookingMapper.toBooking(bookingDtoIn, booking));
        log.info("Бронирование с идентификатором {} создано", booking.getId());
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Override
    public BookingDtoOut approve(long bookingId, Boolean isApproved, long userId) {
        Booking booking = getById(bookingId);
        if( booking.getItem().getOwner().getId() != userId){
            throw new BookingCanBeApprovedOnlyByOwnerException("Подтвердить бронирование может только собственник вещи");
        }

        User owner = getUser(userId);
        Item item = getItem(booking.getItem().getId());
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ItemIsNotAvailableException("Вещь уже забронирована");
        }
        if (owner.getId() != item.getOwner().getId()) {
            throw new IllegalVewAndUpdateException("Подтвердить бронирование может только собственник вещи");
        }
        BookingStatus newBookingStatus = isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newBookingStatus);
        log.info("Бронирование с идентификатором {} обновлено", booking.getId());
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDtoOut getBookingById(long bookingId, long userId) {
        log.info("Получение бронирования по идентификатору {}", bookingId);
        Booking booking = getById(bookingId);
        User booker = booking.getBooker();
        User owner = getUser(booking.getItem().getOwner().getId());
        if (booker.getId() != userId && owner.getId() != userId) {
            throw new IllegalVewAndUpdateException("Только автор или владелец может просматривать данное броинрование");
        }
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoOut> getAllByBooker(String state, long bookerId) {
        User booker = getUser(bookerId);
        List<Booking> bookings;
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerId(booker.getId(), Sort.by(DESC, "start"));
            case CURRENT -> bookingRepository.findAllByBookerIdAndStateCurrent(booker.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case PAST -> bookingRepository.findAllByBookerIdAndStatePast(booker.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case FUTURE -> bookingRepository.findAllByBookerIdAndStateFuture(booker.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case WAITING -> bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                    BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                    BookingStatus.REJECTED, Sort.by(DESC, "end"));
        };
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoOut> getAllByOwner(long ownerId, String state) {
        User owner = getUser(ownerId);
        List<Booking> bookings;
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByOwnerId(owner.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case CURRENT -> bookingRepository.findAllByOwnerIdAndStateCurrent(owner.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case PAST -> bookingRepository.findAllByOwnerIdAndStatePast(owner.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case FUTURE -> bookingRepository.findAllByOwnerIdAndStateFuture(owner.getId(),
                    Sort.by(Sort.Direction.DESC, "start"));
            case WAITING -> bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                    BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
            case REJECTED -> bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                    BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
        };
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Booking getById(long bookingId) {
        log.info("Получение бронирования по идентификатору {}", bookingId);
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", Booking.class)));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", User.class)));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", Item.class)));
    }
}