package com.dang.api.gateway.service;

import java.io.Serializable;

import org.springframework.stereotype.Service;
import com.dang.api.gateway.core.APIMapping;

/**
 * Description:
 *
 * @Author dangfugui  dangfugui@163.cm
 * @Date Create in 2017/10/31
 */
@Service
public class UserServiceImpl {

    @APIMapping("user")
    public User user(User user, int id){
        user.setId(id);
        return user;
    }

    public static class User implements Serializable{
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
