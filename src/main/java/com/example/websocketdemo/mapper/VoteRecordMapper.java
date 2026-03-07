package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VoteRecordMapper {

    VoteRecord findByPollIdAndUserId(@Param("pollId") Long pollId,
                                     @Param("userId") Integer userId);

    void insertVote(VoteRecord voteRecord);

    void deleteByPollId(@Param("pollId") Long pollId);
}
