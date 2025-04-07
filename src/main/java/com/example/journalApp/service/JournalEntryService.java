package com.example.journalApp.service;

import com.example.journalApp.entity.JournalEntry;
import com.example.journalApp.entity.User;
import com.example.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JournalEntryService {
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private UserService userService;

    @Transactional
    //achieves atomicity and isolation as well , if one fails spring rollbacks other changes , if two people call api it will create two transaction containers
    public void saveEntry(JournalEntry myEntry, Optional<String> userName) {
        myEntry.setDate(LocalDateTime.now());
        JournalEntry saved = journalEntryRepository.save(myEntry);
        if(userName.isPresent()) {
            User user = userService.findByUserName(userName.get());
            user.getJournalEntries().add(saved);
            userService.saveEntry(user);
        }
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName) {
        User user = userService.findByUserName(userName);
        boolean removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));
        if(removed) {
            userService.saveEntry(user);
            journalEntryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
