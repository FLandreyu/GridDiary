package com.flandreyu.vo;

import lombok.Data;

/**
 * 标签及其日记数（侧边栏标签云）
 */
@Data
public class TagCountVO {

    /** 标签名 */
    private String name;
    /** 使用该标签的公开日记数 */
    private long count;
}
