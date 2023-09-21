package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

//    员工登陆
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1MD5 加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        2.查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

//      3.判断是否已经查询到了
        if (emp == null){
            return R.error("登录失败");
        }
//      4.密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
//        5.查看员工状态
        if(emp.getStatus()== 0){
            return R.error("账号已经禁用");
        }
//        6.登入成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);

//        return null;
    }
    //员工退出
    @PostMapping("/logout")
    public R<String>logout(HttpServletRequest request){
        //清除当前员工的ID
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    @PostMapping
    public R<String> save (HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //设置初始化密码123456，需要镜像MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    // 员工分页查询
//    自己加的
//    public static boolean isNotEmpty(String str) {
//        return !str.isEmpty();
//    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);
        //构造分页构造器
        Page pageinfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
//        queryWrapper.like(isNotEmpty(name),Employee::getName,name);

        //添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageinfo,queryWrapper);

        return R.success(pageinfo);
    }

    //根据id 修改员工信息
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody  Employee employee){
        log.info(employee.toString());
        //Long empId = (Long) request.getSession().getAttribute("employee");

        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);

        long id = Thread.currentThread().getId();
        log.info("线程id为:{}",id);

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }
    //根据ID 查询员工信息
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("根据id查询员工信息.....");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

}
