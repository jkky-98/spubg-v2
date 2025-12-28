package com.junki.demo.scheduler;

import com.junki.demo.entity.Match;
import com.junki.demo.message.MemberMatchMessage;
import com.junki.demo.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberMatchScheduler {

    private final MatchRepository matchRepository;
    private final RabbitTemplate rabbitTemplate;

    public static final String MEMBER_MATCH_QUEUE = "member.match.queue";

    @Scheduled(fixedDelay = 60000 * 10)
    @Transactional(readOnly = true)
    public void produceMemberMatchTasks() {
        List<Match> unanalyzedMatches = matchRepository.findByAnalysisFalse();

        if (unanalyzedMatches.isEmpty()) return;

        log.info("[MatchAnalysisScheduler] 분석 대상 매치 발견: {}건", unanalyzedMatches.size());

        for (Match match : unanalyzedMatches) {
            MemberMatchMessage message = new MemberMatchMessage(match.getId(), match.getMatchApiId());
            rabbitTemplate.convertAndSend(MEMBER_MATCH_QUEUE, message);
        }
    }
}
