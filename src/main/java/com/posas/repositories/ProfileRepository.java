package com.posas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.posas.entities.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    @Query("FROM Profile profile WHERE profile.email = :email")
    public Profile findByEmail(@Param("email") String email);
}
