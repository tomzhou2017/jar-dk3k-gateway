package com.dk3k.framework.core.web;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dk3k.framework.core.constant.Constants;
import com.dk3k.framework.core.dto.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * Package Name: com.dk3k.framework.web
 * Description:
 * Author: qiuyangjun
 * Create Date:2015/6/17
 */
@RestController
@RequestMapping("error")
public class ErrorController {

    @RequestMapping("403")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity noPermissions(){
        ResponseEntity result = new ResponseEntity();
        result.setMsg(Constants.System.NO_PERMISSIONS_MSG);
        result.setError(Constants.System.NO_PERMISSIONS);
        result.setStatus(Constants.System.FAIL);
        return result;
    }

    @RequestMapping("404")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity noRequestMatch(){
        ResponseEntity result = new ResponseEntity();
        result.setMsg(Constants.System.NO_REQUEST_MATCH_MSG);
        result.setError(Constants.System.NO_REQUEST_MATCH);
        result.setStatus(Constants.System.FAIL);
        return result;
    }

    @RequestMapping("500")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity sysError(){
        ResponseEntity result = new ResponseEntity();
        result.setMsg(Constants.System.SYSTEM_ERROR_MSG);
        result.setError(Constants.System.SYSTEM_ERROR_CODE);
        result.setStatus(Constants.System.FAIL);
        return result;
    }

}
