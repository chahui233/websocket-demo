package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.PollOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PollOptionMapper {

    void insertOption(PollOption option);

    PollOption findByPollIdAndOptionNo(@Param("pollId") Long pollId,
                                       @Param("optionNo") int optionNo);

    List<PollOption> findByPollId(@Param("pollId") Long pollId);

    void incrementVoteCount(@Param("optionId") Long optionId);

    void resetVoteCountByPollId(@Param("pollId") Long pollId);
}
