package com.nlu.app.repository;

import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String> {

}
