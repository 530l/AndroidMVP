package com.lyf.androidmvp.domain.usecase

import com.lyf.androidmvp.core.common.AppException
import com.lyf.androidmvp.core.common.AppResult
import com.lyf.androidmvp.domain.repository.RepoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 收藏状态反转业务用例（非形式主义 UseCase 典型范例）：
 *
 * 【为什么这里必须用 UseCase？】
 * 1. 业务流程闭环：先校验项目存在性 -> 原子计算目标反转状态 -> 写入持久层 -> 输出最新状态。
 * 2. 消除逻辑泄露与重复：HomeViewModel 与 DetailViewModel 之前都重复编写了查库与状态反转逻辑，
 *    通过此 UseCase 将业务决策权完全收敛到 Domain，ViewModel 只负责消费结果。
 * 3. 独立纯单元测试：可以脱离 Android/Room 环境针对边界（如数据不存在、状态翻转）单独测试业务逻辑。
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repoRepository: RepoRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Boolean> {
        val currentRepo = repoRepository.getRepoById(id).first()
            ?: return AppResult.Error(AppException.Business("项目不存在或已被移除"))

        val targetStatus = !currentRepo.isFavorite
        return try {
            repoRepository.toggleFavorite(id = id, isFavorite = targetStatus)
            AppResult.Success(targetStatus)
        } catch (e: Exception) {
            AppResult.Error(AppException.Unknown(e.localizedMessage ?: "更新收藏状态失败", e))
        }
    }
}
