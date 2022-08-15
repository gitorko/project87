package com.demo.project87;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.demo.project87.domain.Ticket;
import com.demo.project87.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class Main implements CommandLineRunner {

    @Autowired
    TicketRepository ticketRepo;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding test data!");
        List<String> city = Arrays.asList("London", "New York", "Bangalore");
        ticketRepo.deleteAll();
        IntStream.range(1, 51).forEach(i -> {
            ticketRepo.save(Ticket.builder()
                    .seatNumber("Seat_" + i)
                    .eventDate(LocalDate.now())
                    .lockedBy("")
                    .bookedBy("")
                    .price(10.0)
                    .build());
        });
    }
}
