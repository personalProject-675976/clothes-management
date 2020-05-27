package com.lyx.process.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.common.CommonResult;
import com.lyx.common.CommonUtil;
import com.lyx.config.QiniuOSS;
import com.lyx.dto.ClotheSaveDto;
import com.lyx.entity.Clothes;
import com.lyx.process.mapper.ClothesMapper;
import com.lyx.process.service.IClothesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 衣物表 服务实现类
 * </p>
 *
 * @author powered by lyx
 * @since 2020-05-27
 */
@Service("clothesServiceImpl")
public class ClothesServiceImpl extends ServiceImpl<ClothesMapper, Clothes> implements IClothesService
{
	@Autowired
	@Qualifier("qiniuOSS")
	private QiniuOSS qiniuOSS;

	@Override
	public CommonResult save(ClotheSaveDto dto)
	{
		if (StrUtil.isBlank(dto.getCName()))
		{
			return CommonResult.errorMsg("名称不能为空");
		}
		if (!CommonUtil.kindIsOk(dto.getKind()))
		{
			return CommonResult.errorMsg("种类不正确");
		}
		if (Objects.isNull(dto.getFile()))
		{
			return CommonResult.errorMsg("请上传文件");
		}
		if (!CommonUtil.isPicFile(dto.getFile().getOriginalFilename()))
		{
			return CommonResult.errorMsg("上传的不是图片");
		}

		try
		{
			String url = qiniuOSS.uploadClothesPic(dto.getFile());
			if (StrUtil.isBlank(url))
			{
				return CommonResult.errorMsg("文件上传失败");
			}
			int sequence = this.getLastSequence(dto.getKind()) + 1;

			Clothes clothes = new Clothes();
			clothes.setCName(dto.getCName());
			clothes.setKind(dto.getKind());
			clothes.setUrl(url);
			clothes.setSequence(sequence);

			return this.save(clothes) ? CommonResult.successMsg("添加成功") : CommonResult.errorMsg("添加失败");
		}
		catch (Exception e)
		{
			System.out.println("添加衣物失败，错误信息：" + e.getMessage());
			return CommonResult.errorMsg("添加衣物失败");
		}
	}

	@Override
	public CommonResult remove(int id)
	{
		// 获得key
		Clothes clothes = this.getById(id);
		if (Objects.isNull(clothes))
		{
			return CommonResult.errorMsg("衣物不存在");
		}
		String url = clothes.getUrl();
		String key = url.substring(url.lastIndexOf("clothes"));

		// 删除文件
		if (!qiniuOSS.delete(key))
		{
			return CommonResult.errorMsg("删除文件失败");
		}

		// 删除记录
		this.removeById(id);

		return CommonResult.successMsg("删除成功");
	}

	@Override
	public CommonResult listByKind(int kind)
	{
		if (!CommonUtil.kindIsOk(kind))
		{
			return CommonResult.errorMsg("种类不正确");
		}
		List<Clothes> list = new LambdaQueryChainWrapper<>(this.baseMapper)
								.eq(Clothes::getKind, kind)
								.orderByAsc(Clothes::getSequence)
								.list();

		return CommonResult.successData(list);
	}

	/**
	 * 获得某一类的最大排序编号
	 */
	private int getLastSequence(int kind)
	{
		List<Clothes> list = new LambdaQueryChainWrapper<>(this.baseMapper)
								.eq(Clothes::getKind, kind)
								.orderBy(true, true, Clothes::getSequence)
								.list();
		if (list.isEmpty())
		{
			return 0;
		}
		return list.get(list.size()-1).getSequence();
	}
}