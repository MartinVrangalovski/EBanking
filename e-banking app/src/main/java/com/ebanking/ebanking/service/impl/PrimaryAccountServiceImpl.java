package com.ebanking.ebanking.service.impl;

import com.ebanking.ebanking.model.PrimaryAccount;
import com.ebanking.ebanking.model.User;
import com.ebanking.ebanking.model.exceptions.NotSupportedTransferException;
import com.ebanking.ebanking.model.exceptions.PrimaryAccountNotFoundException;
import com.ebanking.ebanking.model.exceptions.UserNotFoundException;
import com.ebanking.ebanking.repository.PrimaryAccountRepository;
import com.ebanking.ebanking.repository.UserRepository;
import com.ebanking.ebanking.service.PrimaryAccountService;
import com.ebanking.ebanking.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrimaryAccountServiceImpl implements PrimaryAccountService {

    private final PrimaryAccountRepository primaryAccountRepository;
    private final UserRepository userRepository;
    private UserService userService;

    public PrimaryAccountServiceImpl(PrimaryAccountRepository primaryAccountRepository, UserRepository userRepository) {
        this.primaryAccountRepository = primaryAccountRepository;
        this.userRepository = userRepository;
    }


    @Override
    public PrimaryAccount createAccount(String username, int number, Double ballance) {
        PrimaryAccount primaryAccount = new PrimaryAccount(username, number, ballance);
        return this.primaryAccountRepository.save(primaryAccount);
    }

    @Override
    public Optional<PrimaryAccount> getAccount(Long id) {
        PrimaryAccount primaryAccount = this.primaryAccountRepository.findById(id)
                .orElseThrow(() -> new PrimaryAccountNotFoundException(id));
        return Optional.of(primaryAccount);
    }

    @Override
    public User addMoneyToAccount(Long id, Double ballance) {
        PrimaryAccount primaryAccount = this.getAccount(id).get();
        primaryAccount.setAccountBalance(primaryAccount.getAccountBalance() + ballance);
        this.primaryAccountRepository.save(primaryAccount);
        User user = this.userRepository.findByUsername(primaryAccount.getUsername())
                .orElseThrow(() -> new UserNotFoundException(primaryAccount.getUsername()));
        user.setPrimaryAccount(primaryAccount);
        this.userRepository.save(user);
        return user;
    }

    @Override
    public List<PrimaryAccount> findAll() {
        return this.primaryAccountRepository.findAll();
    }

    @Override
    public PrimaryAccount transferMoney(Long fromAccountId, Long toAccountId, Double transferAmount) {
        PrimaryAccount fromAccount = this.primaryAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new PrimaryAccountNotFoundException(fromAccountId));
        PrimaryAccount toAccount = this.primaryAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new PrimaryAccountNotFoundException(toAccountId));
        if (fromAccount.getAccountBalance() - transferAmount < 0){
            throw new NotSupportedTransferException(fromAccount.getNumber());
        }

        fromAccount.setAccountBalance(fromAccount.getAccountBalance() - transferAmount);
        toAccount.setAccountBalance(toAccount.getAccountBalance() + transferAmount);

        this.primaryAccountRepository.save(fromAccount);
        this.primaryAccountRepository.save(toAccount);
        return fromAccount;
    }
}
