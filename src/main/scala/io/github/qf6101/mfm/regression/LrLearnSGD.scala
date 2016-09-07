package io.github.qf6101.mfm.regression

import breeze.linalg.SparseVector
import io.github.qf6101.mfm.base.{MLLearner, MLModel}
import io.github.qf6101.mfm.optimization.{GradientDescent, Updater}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

/**
  * Created by qfeng on 15-3-17.
  */

/**
  * Train a classification model for Logistic Regression using Stochastic Gradient Descent. By
  * default L2 regularization is used, which can be changed via
  * [[LrLearnSGD]].
  */

/**
  * 逻辑斯蒂模型的SGD学习器
  * @param paramPool 参数池*
  * @param updater 参数更新器
  * @param initialCoeffs 初始参数
  */
class LrLearnSGD(override val paramPool: ParamMap,
                 val updater: Updater,
                 val initialCoeffs: Option[VectorCoefficients] = None)
  extends MLLearner(paramPool) with LrModelParam {
  val lg = new LogisticGradient(paramPool)
  val gd = new GradientDescent(lg, updater, paramPool)

  /**
    * 训练逻辑斯蒂模型
    * @param dataSet 训练集
    * @return 逻辑斯蒂模型
    */
  override def train(dataSet: RDD[(Double, SparseVector[Double])]): MLModel = {
    dataSet.persist(StorageLevel.MEMORY_AND_DISK_SER_2)
    val inputCoeffs = initialCoeffs match {
      case Some(value) => value
      case None => new VectorCoefficients(dataSet.first()._2.length)
    }
    val coeffs = gd.optimize(dataSet, inputCoeffs, paramPool(reg))
    dataSet.unpersist()
    new LrModel(coeffs.asInstanceOf[VectorCoefficients], this, paramPool)
  }


}

/**
  * 逻辑斯蒂模型的SGD学习器实例
  */
object LrLearnSGD {
  /**
    * 训练逻辑斯蒂模型
    *
    * @param dataset 数据集
    * @param paramPool 参数池*
    * @param updater 参数更新器
    * @param initialCoeffs 初始参数
    * @return 逻辑斯蒂模型
    */
  def train(dataset: RDD[(Double, SparseVector[Double])],
            paramPool: ParamMap,
            updater: Updater,
            initialCoeffs: Option[VectorCoefficients] = None): MLModel = {
    new LrLearnSGD(paramPool, updater, initialCoeffs).train(dataset)
  }
}