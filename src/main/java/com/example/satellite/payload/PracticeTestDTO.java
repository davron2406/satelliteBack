package com.example.satellite.payload;




import com.example.satellite.entity.PracticeTest;
import com.example.satellite.entity.PracticeTestQuestion;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PracticeTestDTO {
    public UUID id; public String title;
    public UUID userId;
    public Integer timeLimitSec; public Instant startedAt; public Instant endsAt; public String status;
    public boolean shuffle; public boolean allowPartial;
    public int totalQuestions;
    public List<UUID> questionIds;


    public static PracticeTestDTO of(PracticeTest t, List<PracticeTestQuestion> links){
        PracticeTestDTO d = new PracticeTestDTO();
        d.id = t.getId(); d.title = t.getTitle();
        d.userId = t.getUser()==null ? null : t.getUser().getId();
        d.timeLimitSec = t.getTimeLimitSec(); d.startedAt = t.getStartedAt(); d.endsAt = t.getEndsAt();
        d.status = t.getStatus()==null?null:t.getStatus().name();
        d.shuffle = t.isShuffle(); d.allowPartial = t.isAllowPartial();

        d.questionIds = links.stream().sorted((a,b)->Integer.compare(a.getPosition(), b.getPosition()))
                .map(l -> l.getQuestion().getId()).toList();
        d.totalQuestions = d.questionIds.size();

//        d.topicIds = links.stream().map(l -> l.getQuestion().getTopic().getId()).distinct().collect(Collectors.toList());
        return d;
    }
}
