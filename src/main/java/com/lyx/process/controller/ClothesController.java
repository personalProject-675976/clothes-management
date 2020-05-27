package com.lyx.process.controller;


import com.lyx.common.CommonResult;
import com.lyx.dto.ClotheSaveDto;
import com.lyx.process.service.IClothesService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 衣物表 前端控制器
 * </p>
 *
 * @author powered by lyx
 * @since 2020-05-27
 */
@RestController
@RequestMapping("/clothes")
public class ClothesController
{
	@Autowired
	@Qualifier("clothesServiceImpl")
	private IClothesService service;

	@PostMapping("/save")
	@ApiOperation("添加一件衣物")
	public CommonResult save(ClotheSaveDto dto)
	{
		return service.save(dto);
	}

	@Delete("/remove")
	@ApiModelProperty("删除某个衣物")
	public CommonResult remove(@RequestParam int id)
	{
		return service.remove(id);
	}

	@GetMapping("/list/{kind}")
	@ApiOperation("获得某类衣物")
	public CommonResult listByKind(@PathVariable("kind") int kind)
	{
		return service.listByKind(kind);
	}
}