package com.lyf.androidmvp.domain.usecase

import com.lyf.androidmvp.core.common.AppException
import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.domain.repository.RepoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 项目备注保存业务用例（业务规则前置校验与脏检查范例）：
 *
 * 【为什么这里必须用 UseCase？】
 * 1. 业务不变量约束：空白字符修剪（trim）与 200 字符上限强校验，保护领域数据质量。
 * 2. 脏数据拦截（Dirty Checking）：如果新备注与已有内容完全一致，短路返回 Success，避免无意义的数据库 I/O。
 * 3. 业务规则复用：无论未来是从详情页、快捷弹窗还是批量操作更新备注，校验规则唯一。
 */
class SaveRepoRemarkUseCase @Inject constructor(
    private val repoRepository: RepoRepository
) {
    companion object {
        const val MAX_REMARK_LENGTH = 200
    }

    suspend operator fun invoke(id: Long, remark: String): AppResult<Unit> {
        val cleanRemark = remark.trim()
        if (cleanRemark.length > MAX_REMARK_LENGTH) {
            return AppResult.Error(AppException.Business("备注长度不能超过 $MAX_REMARK_LENGTH 个字符"))
        }

        val currentRepo = repoRepository.getRepoById(id).first()
            ?: return AppResult.Error(AppException.Business("项目不存在或已被移除"))

        // 脏检查：内容未变更时不触发底层持久化
        if (currentRepo.userRemark.orEmpty() == cleanRemark) {
            return AppResult.Success(Unit)
        }

        return try {
            repoRepository.saveRemark(id = id, remark = cleanRemark)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Unknown(e.localizedMessage ?: "保存备注失败", e))
        }
    }
}
