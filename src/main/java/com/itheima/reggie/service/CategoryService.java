package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;
import org.springframework.stereotype.Controller;

//@Controller
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
