/*
 * Copyright 2019, Jeziel Lago - Alex Soares.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.flownav.router

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavAction
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.flownav.router.extension.getGraphOr

fun FlowNavFragmentRouter.workWithNavGraphOf(
    @IdRes navHost: Int,
    activity: FragmentActivity,
    navigateFragment: FlowNavFragmentRouter.() -> Unit
) {
    val navHostFragment = activity.supportFragmentManager.findFragmentById(navHost) as NavHostFragment

    val navGraph = navHostFragment.navController.getGraphOr {
        NavGraph(NavGraphNavigator(NavigatorProvider()))
    }

    val navFrag = this
    navFrag.navigateFragment()

    navFrag.fragmentsToAdd.forEach {
        navGraph.addDestination(
            FragmentNavigator(
                activity,
                activity.supportFragmentManager,
                navHost
            ).createDestination().apply {
                id = it.key
                className = it.value.className

                it.value.actions.forEach {
                    putAction(it.key, NavAction(it.value))
                }
            })
    }

    navFrag.startDestination?.let { navGraph.startDestination = it }

    navHostFragment.navController.graph = navGraph
    cleanRouter(navFrag)
}

fun FlowNavFragmentRouter.navigateTo(destination: String, lifecycleOwner: LifecycleOwner) {

    val navController: NavController? = when(lifecycleOwner) {
        is Fragment -> {
            lifecycleOwner.findNavController()
        }
        is FragmentActivity -> {
            lifecycleOwner.supportFragmentManager.fragments.first().findNavController()
        }
        else -> {
            error("Navigate only on Fragment or FragmentActivity")
        }
    }

    FlowNavApp.getFragmentMap()[destination]?.second?.let {
        navController?.navigate(it)
    }
}

internal fun cleanRouter(navigateFragment: FlowNavFragmentRouter) {
    navigateFragment.fragmentsToAdd.clear()
    navigateFragment.startDestination = null
}