package com.demo.project87.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.transaction.Transactional;

import com.demo.project87.domain.BookingRequest;
import com.demo.project87.domain.Ticket;
import com.demo.project87.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private static final Integer EXPIRY_TTL_SECS = 30;

    @Autowired
    TicketRepository ticketRepo;

    @GetMapping(value = "/api/user")
    public String getUser() {
        return UUID.randomUUID().toString().substring(0, 7);
    }

    @GetMapping(value = "/api/tickets")
    public Iterable<Ticket> getTickets() {
        return ticketRepo.findAllByOrderByIdAsc();
    }

    @PostMapping(value = "/api/ticket")
    public Boolean bookTicket(@RequestBody BookingRequest bookingRequest) {
        log.info("Confirming Booking! {}", bookingRequest);
        return confirmBooking(bookingRequest);
    }

    @PostMapping(value = "/api/hold")
    public Boolean holdBooking(@RequestBody BookingRequest bookingRequest) {
        log.info("Holding booking tickets! {}", bookingRequest);
        return bookingHoldCall(bookingRequest, true);
    }

    @PostMapping(value = "/api/cancel")
    public Boolean cancelBooking(@RequestBody BookingRequest bookingRequest) {
        log.info("Cancelling booking tickets! {}", bookingRequest);
        return bookingHoldCall(bookingRequest, false);
    }

    @GetMapping(value = "/api/admit/{entryToken}")
    public String admit(@PathVariable String entryToken) {
        Ticket ticket = ticketRepo.findByEntryTokenIs(entryToken);
        if (ticketRepo.findByEntryTokenIs(entryToken) != null) {
            ticket.setEntered(true);
            ticketRepo.save(ticket);
            return "ADMIT";
        } else {
            return "INVALID";
        }
    }

    @GetMapping(value = "/api/qrcode/{entryToken}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getQRCode(@PathVariable String entryToken) {
        Ticket ticket = ticketRepo.findByEntryTokenIs(entryToken);
        if (ticketRepo.findByEntryTokenIs(entryToken) != null) {
            return ticket.getQrCode();
        } else {
            return null;
        }
    }

    @Transactional
    public Boolean confirmBooking(BookingRequest bookingRequest) {
        try {
            Iterable<Ticket> ticketSet = ticketRepo.findAllById(bookingRequest.getTicketIds());
            Set<Ticket> tickets = new HashSet<>();
            for (Ticket ticket : ticketSet) {
                tickets.add(ticket);
                //Only person who held the lock can complete the booking.
                if (ticket.getLockedBy().equals(bookingRequest.getUser())) {
                    ticket.setLockedBy("");
                    ticket.setBooked(true);
                    ticket.setBookedBy(bookingRequest.getUser());

                    //Create the QR code for the ticket and store to DB.
                    String entryToken = UUID.randomUUID().toString();
                    ticket.setEntryToken(entryToken);
                    String hostName = InetAddress.getLocalHost().getHostAddress();
                    String entryUri = "http://" + hostName + ":8080/api/admit/" + entryToken;
                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    BitMatrix bitMatrix = qrCodeWriter.encode(entryUri, BarcodeFormat.QR_CODE, 200, 200);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
                        byte[] png = baos.toByteArray();
                        ticket.setQrCode(png);
                    }

                } else {
                    log.info("Ticket: {} lock is held by other user!", ticket);
                    return false;
                }
            }
            ticketRepo.saveAll(tickets);
            return true;
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error("Booking confirmation failed due to lock, {}", ex.getMessage());
            return false;
        } catch (WriterException | IOException ex) {
            log.error("Failed to generate QR code, {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Booking confirmation failed, {}", ex.getMessage());
            return false;
        }

    }

    @Transactional
    public Boolean bookingHoldCall(BookingRequest bookingRequest, Boolean start) {
        try {
            Iterable<Ticket> ticketSet = ticketRepo.findAllById(bookingRequest.getTicketIds());
            Set<Ticket> tickets = new HashSet<>();
            for (Ticket ticket : ticketSet) {
                tickets.add(ticket);
                //Reserve the ticket till the time payment is done.
                if (start) {
                    if (ticket.getBooked()) {
                        log.info("Ticket: {} is already booked!", ticket);
                        return false;
                    }
                    //Only if ticket is free it can be booked.
                    if (ticket.getLockedBy().equals("")) {
                        ticket.setLockedBy(bookingRequest.getUser());
                        //TTL to release lock after 30 seconds.
                        ticket.setLockExpiry(LocalDateTime.now().plusSeconds(EXPIRY_TTL_SECS));
                        log.info("Ticket: {} is reserved!", ticket);
                    } else {
                        log.info("Ticket: {} is already locked by other user!", ticket);
                        return false;
                    }
                } else {
                    //Only person who held the lock can release it.
                    if (ticket.getLockedBy().equals(bookingRequest.getUser())) {
                        ticket.setLockedBy("");
                        log.info("Ticket: {} is released!", ticket);
                    } else {
                        log.info("Ticket: {} is already locked by other user!", ticket);
                        return false;
                    }
                }
            }
            ticketRepo.saveAll(tickets);
            return true;
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error("Error reserving flow: {}", ex.getMessage());
            return false;
        }

    }

    //Runs every 1 min.
    @Scheduled(fixedRate = 60000)
    public void scheduleFixedRateTask() {
        log.info("Running lock cleanup job!");
        Iterable<Ticket> ticketSet = ticketRepo.findAllByLockExpiryIsNotNull();
        Set<Ticket> tickets = new HashSet<>();
        ticketSet.forEach(t -> {
            if (t.getLockExpiry().isBefore(LocalDateTime.now())) {
                t.setLockedBy("");
                t.setLockExpiry(null);
                ticketRepo.save(t);
                log.info("Ticket: {} lock released!", t);
            }
        });
        log.info("Lock cleanup job completed!");
    }

}
