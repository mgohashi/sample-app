package io.mohashi.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.mohashi.model.User;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class UserService {
    public Uni<List<User>> list() {
        return User.findAll().list();
    }
}
