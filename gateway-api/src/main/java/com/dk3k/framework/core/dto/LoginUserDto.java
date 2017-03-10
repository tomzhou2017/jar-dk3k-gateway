package com.dk3k.framework.core.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.dk3k.framework.core.dto.ResourcesDto;
import com.dk3k.framework.core.dto.RolesDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by zhangyingliang on 2016/12/10.
 * <p>
 * install of artifactId : mobanker-fx-contract
 *
 * @see{com.dk3k.framework.contract.dto.ResourcesDto} 下个版本删除，请注意！这样名称的结构是模块化，有规律，方便spring集成scan指定模块的包，
 * 而不是像之前扫 com.dk3k.framework.**
 * 而现在是按需加载，需要什么模块扫什么模块，之间不相互依赖  比如扫：com.dk3k.framework.contract ，com.dk3k.framework.dao ，
 * 这样做其实实质是简化开发，你自己体会吧！core 也需要改，但是只能一点一点割肉了....因为原先老系统依赖太多，结构也有些不合理
 */

@Deprecated
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class LoginUserDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

    private Long userId;

    private String userName;

    private String realName;

    private Date loginTime;

    private String loginIp;

    List<ResourcesDto> resourcesList;
    List<RolesDto> rolesList;
}
