package com.ruoyi.system.domain.bo;

public class ApprovalHandleRequest
{
    private Long taskId;

    private String comment;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
