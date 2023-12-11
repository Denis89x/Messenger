package by.lebenkov.messenger.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
public class Account {
    @Id
    @Column(name = "id_account")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAccount;

    @Size(min = 3, max = 12, message = "Логин должен быть длины от 3 до 12 символов")
    @NotEmpty(message = "Логин не должен быть пустым")
    @Column(name = "username")
    private String username;
    
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Size(min = 10, max = 30, message = "Адрес почты должен корректным")
    @NotEmpty(message = "Адрес почты должен корректным")
    @Email()
    @Column(name = "email")
    private String email;

    @Column(name = "is_verified_email")
    private Boolean isVerifiedEmail;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "profile_picture")
    private String profilePicture;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConversationParticipant> conversations = new ArrayList<>();
}