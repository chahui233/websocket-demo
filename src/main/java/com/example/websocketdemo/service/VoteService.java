package com.example.websocketdemo.service;

import com.example.websocketdemo.mapper.PollMapper;
import com.example.websocketdemo.mapper.PollOptionMapper;
import com.example.websocketdemo.mapper.UserMapper;
import com.example.websocketdemo.mapper.VoteRecordMapper;
import com.example.websocketdemo.model.Poll;
import com.example.websocketdemo.model.PollOption;
import com.example.websocketdemo.model.User;
import com.example.websocketdemo.model.VoteRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VoteService {

    private static final int DEFAULT_OPTION_COUNT = 3;

    @Autowired
    private PollMapper pollMapper;

    @Autowired
    private PollOptionMapper pollOptionMapper;

    @Autowired
    private VoteRecordMapper voteRecordMapper;

    @Autowired
    private UserMapper userMapper;

    private final Map<String, Integer> onlineByRoom = new ConcurrentHashMap<>();

    @Transactional
    public boolean vote(String roomId, String username, int optionNo) {

        Poll poll = ensurePollExists(roomId);

        if (!Boolean.TRUE.equals(poll.getVoting())) {
            return false;
        }

        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }

        PollOption option = pollOptionMapper.findByPollIdAndOptionNo(poll.getId(), optionNo);
        if (option == null) {
            return false;
        }

        VoteRecord existing = voteRecordMapper.findByPollIdAndUserId(poll.getId(), user.getId());
        if (existing != null) {
            return false;
        }

        VoteRecord voteRecord = new VoteRecord();
        voteRecord.setPollId(poll.getId());
        voteRecord.setOptionId(option.getId());
        voteRecord.setUserId(user.getId());
        voteRecordMapper.insertVote(voteRecord);

        pollOptionMapper.incrementVoteCount(option.getId());

        return true;
    }

    @Transactional
    public void startVote(String roomId) {

        Poll poll = ensurePollExists(roomId);

        pollMapper.updateVotingStatus(poll.getId(), true);
        pollMapper.updateStartTime(poll.getId());

        pollOptionMapper.resetVoteCountByPollId(poll.getId());
        voteRecordMapper.deleteByPollId(poll.getId());
    }

    @Transactional
    public void stopVote(String roomId) {

        Poll poll = ensurePollExists(roomId);

        pollMapper.updateVotingStatus(poll.getId(), false);
        pollMapper.updateEndTime(poll.getId());
    }

    public Map<Integer, Integer> getResult(String roomId) {

        Poll poll = ensurePollExists(roomId);
        List<PollOption> options = pollOptionMapper.findByPollId(poll.getId());

        Map<Integer, Integer> result = new HashMap<>();
        for (int i = 1; i <= DEFAULT_OPTION_COUNT; i++) {
            result.put(i, 0);
        }

        for (PollOption option : options) {
            result.put(option.getOptionNo(), option.getVoteCount());
        }

        return result;
    }

    public boolean isVoting(String roomId) {

        Poll poll = ensurePollExists(roomId);
        return Boolean.TRUE.equals(poll.getVoting());
    }

    public void addUser(String roomId) {
        onlineByRoom.merge(roomId, 1, Integer::sum);
    }

    public void removeUser(String roomId) {
        onlineByRoom.compute(roomId, (key, count) -> {
            if (count == null || count <= 1) {
                return null;
            }
            return count - 1;
        });
    }

    public int getOnline(String roomId) {
        return onlineByRoom.getOrDefault(roomId, 0);
    }

    @Transactional
    protected Poll ensurePollExists(String roomId) {

        Poll poll = pollMapper.findByRoomId(roomId);
        if (poll != null) {
            return poll;
        }

        Poll newPoll = new Poll();
        newPoll.setRoomId(roomId);
        newPoll.setTitle("房间 " + roomId + " 投票");
        newPoll.setDescription("默认投票主题");
        newPoll.setVoting(false);
        pollMapper.insertPoll(newPoll);

        for (int i = 1; i <= DEFAULT_OPTION_COUNT; i++) {
            PollOption option = new PollOption();
            option.setPollId(newPoll.getId());
            option.setOptionNo(i);
            option.setContent("选项" + i);
            option.setVoteCount(0);
            pollOptionMapper.insertOption(option);
        }

        return newPoll;
    }
}
