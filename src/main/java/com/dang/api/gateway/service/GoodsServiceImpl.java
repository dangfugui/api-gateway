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
public class GoodsServiceImpl {

    @APIMapping("addGoods")
    public Goods addGoods(Goods goods, Integer id){
        return goods;
    }

    public static class Goods implements Serializable{
        private String googlesName;
        private String goodsId;

        public String getGooglesName() {
            return googlesName;
        }

        public void setGooglesName(String googlesName) {
            this.googlesName = googlesName;
        }

        public String getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }
    }
}
