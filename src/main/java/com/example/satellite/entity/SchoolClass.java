package com.example.satellite.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "school_classes")
public class SchoolClass extends AbstractEntity {


    @Column(nullable = false, unique = true, length = 80)
    private String name;

    // Teachers assigned to this class (must be users with role TEACHER)
    @ManyToMany
    @JoinTable(
            name = "class_teachers",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> teachers = new HashSet<>();

    // Students in this class (must be users with role STUDENT)
    @ManyToMany
    @JoinTable(
            name = "class_students",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> students = new HashSet<>();

}
