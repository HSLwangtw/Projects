/*
 Copyright (c) 2014 by Contributors 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
    
 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.dmlc.xgboost4j.demo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dmlc.xgboost4j.Booster;
import org.dmlc.xgboost4j.DMatrix;
import org.dmlc.xgboost4j.demo.util.CustomEval;
import org.dmlc.xgboost4j.demo.util.Params;
import org.dmlc.xgboost4j.util.Trainer;
import org.dmlc.xgboost4j.util.XGBoostError;

/**
 * this is an example of fit generalized linear model in xgboost
 * basically, we are using linear model, instead of tree for our boosters
 * @author hzx
 */
public class GeneralizedLinearModel {
    public static void main(String[] args) throws XGBoostError {
        // load file from text file, also binary buffer generated by xgboost4j
        DMatrix trainMat = new DMatrix("../../demo/data/agaricus.txt.train");
        DMatrix testMat = new DMatrix("../../demo/data/agaricus.txt.test");
        
        //specify parameters
        //change booster to gblinear, so that we are fitting a linear model
        // alpha is the L1 regularizer
        //lambda is the L2 regularizer
        //you can also set lambda_bias which is L2 regularizer on the bias term
        Params param = new Params() {
            {
                put("alpha", 0.0001);
                put("silent", 1);
                put("objective", "binary:logistic");
                put("booster", "gblinear");
                put("eval_metric", "auc");
            }
        };
        //normally, you do not need to set eta (step_size)
        //XGBoost uses a parallel coordinate descent algorithm (shotgun),
        //there could be affection on convergence with parallelization on certain cases
        //setting eta to be smaller value, e.g 0.5 can make the optimization more stable
        //param.put("eta", "0.5");
        
        
        //specify watchList
        List<Map.Entry<String, DMatrix>> watchs =  new ArrayList<>();
        watchs.add(new AbstractMap.SimpleEntry<>("train", trainMat));
        watchs.add(new AbstractMap.SimpleEntry<>("test", testMat));
        
        //train a booster
        int round = 4;
        Booster booster = Trainer.train(param, trainMat, round, watchs, null, null);
        
        float[][] predicts = booster.predict(testMat);
        
        CustomEval eval = new CustomEval();
        
        System.out.println("error=" + eval.eval(predicts, testMat));
    }
}
