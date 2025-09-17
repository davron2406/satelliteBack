package com.example.satellite.payload;



import java.time.OffsetDateTime;
import java.util.UUID;

public class SubmitPracticeTestResponse {
    public UUID resultId;
    public UUID practiceTestId;
    public UUID templateId;
    public int correct;
    public int total;
    public double percent;
    public OffsetDateTime submittedAt;

    public SubmitPracticeTestResponse(UUID resultId, UUID practiceTestId, UUID templateId,
                                      int correct, int total, OffsetDateTime submittedAt) {
        this.resultId = resultId;
        this.practiceTestId = practiceTestId;
        this.templateId = templateId;
        this.correct = correct;
        this.total = total;
        this.percent = total == 0 ? 0.0 : (100.0 * correct / total);
        this.submittedAt = submittedAt;
    }
}

