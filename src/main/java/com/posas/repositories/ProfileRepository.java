package com.posas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posas.entities.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
