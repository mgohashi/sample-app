package io.mohashi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import io.mohashi.fixture.PanacheEntityBaseWithFixture;
import io.mohashi.service.UserSearch;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.vertx.mutiny.sqlclient.Row;

@Entity
@Table(name = "USERS")
public class User extends PanacheEntityBaseWithFixture {	

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name="users_seq", sequenceName = "user_id_seq")
    private Long id;
    private String name;
    private String email;
    
    public User() {}

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + "]";
    }

    public static User from(Row row) {
        return new User(row.getLong("id"),
        row.getString("name"),
        row.getString("email"));
    }

    public static PanacheQuery<PanacheEntityBase> findBySearch(UserSearch userSearch) {
        switch (userSearch.getType()) {
            case NAME: 
                return User.find("name like :name", 
                    Parameters.with("name", 
                        wrap(userSearch.name().get())));
            case EMAIL:
                return User.find("email like :email", 
                    Parameters.with("email", 
                        wrap(userSearch.email().get())));
            case ALL:
                return User.find("name like :name and email like :email", 
                    Parameters.with("name", 
                        wrap(userSearch.name().get()))
                            .and("email", wrap(userSearch.email().get())));
            default:
                return User.findAll();
        }
    }

    private static Object wrap(String string) {
        return "%" + string + "%";
    }

}
