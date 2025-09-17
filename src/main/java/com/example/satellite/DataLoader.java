package com.example.satellite;

import com.example.satellite.repository.RoleRepository;
import com.example.satellite.repository.TopicRepository;
import com.example.satellite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private TopicRepository topicRepository;



    @Override
    public void run(String... args) throws Exception {
//        Permission[] permissions = Permission.values();
//        Role admin = roleRepository.save(new Role("ADMIN", Arrays.asList(permissions)));
//        Role teacher = roleRepository.save(new Role("TEACHER", Arrays.asList(permissions)));
//        Role student = roleRepository.save(new Role("STUDENT", Arrays.asList(permissions)));
//
//        userRepository.save(new User("Admin", "admin","admin123@gmail.com","944288899", passwordEncoder.encode( "admin123"),admin, "123",1, true));
//
//
//        for( int i = 0;i < 100; i++ ) {
//
//            userRepository.save(new User("User" + i, "user" + i,"user" + i +"@gmail.com","9442888" + i, passwordEncoder.encode( "user" + i),student,"123",1, true));
//
//        }
//
//        for( int i = 0;i < 10; i++ ) {
//
//            userRepository.save(new User("Teacher" + i, "teacher" + i,"teacher" + i +"@gmail.com","944288" + i, passwordEncoder.encode( "teacher" + i),teacher,"123",1, true));
//
//        }


//
//        Sidebar sideBarMenu = new Sidebar("Practice Tests to solve","practiceTestsToSolve","question_mark");
//        Sidebar sideBarMenu1 = new Sidebar("Practice Tests","practiceTests","assignment");
//        Sidebar sideBarMenu3 = new Sidebar("Topics","topics","topic");
//        Sidebar sideBarMenu4 = new Sidebar("Questions","questions","questions");
//        sidebarRepository.save(sideBarMenu);
//        sidebarRepository.save(sideBarMenu1);
//        sidebarRepository.save(sideBarMenu3);
//        sidebarRepository.save(sideBarMenu4);
//
//        Topic topic = new Topic("Functions", "", "SAT_MATH");
//        Topic topic1 = new Topic("Essential Non Essential", "", "SAT_WRITING");
//        Topic topic2 = new Topic("WORD PROBLEMS", "", "SAT_MATH");
//        Topic topic3 = new Topic("IDEAS", "", "SAT_READING");
//        Topic topic4 = new Topic("COMMAS", "", "SAT_WRITING");
//
//        topicRepository.save(topic1);
//        topicRepository.save(topic2);
//        topicRepository.save(topic3);
//        topicRepository.save(topic4);
//        topicRepository.save(topic);
//
//        PracticeTestDetails practiceTestDetails = new PracticeTestDetails();
//        practiceTestDetails.setName("Practice Test 2");
//        practiceTestDetails.setTopics(Arrays.asList(UUID.fromString("9702fb8a-434b-45c0-96b3-f3af427c64fc"), UUID.fromString("2116c6fa-0e9b-4466-be30-aa8a30357bdd")));
//        practiceTestDetails.setQuestionLevels(Arrays.asList(0,0));
//        practiceTestDetails.setNumberOfQuestions(Arrays.asList(5, 2));
//
//        practiceTestService.generatePracticeTest(practiceTestDetails);
    }
}
