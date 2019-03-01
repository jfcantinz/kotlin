///*
// * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
// * that can be found in the license/LICENSE.txt file.
// */
//
//package org.jetbrains.kotlin.fir.resolve.transformers
//
//import org.jetbrains.kotlin.fir.FirSession
//import org.jetbrains.kotlin.fir.declarations.*
//import org.jetbrains.kotlin.fir.render
//import org.jetbrains.kotlin.fir.resolve.toSymbol
//import org.jetbrains.kotlin.fir.symbols.ConeClassifierSymbol
//import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
//import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
//import org.jetbrains.kotlin.fir.types.ConeClassLikeType
//import org.jetbrains.kotlin.fir.types.FirTypeRef
//import org.jetbrains.kotlin.fir.types.coneTypeSafe
//import org.jetbrains.kotlin.fir.types.impl.FirErrorTypeRefImpl
//import org.jetbrains.kotlin.fir.visitors.CompositeTransformResult
//import org.jetbrains.kotlin.fir.visitors.compose
//import org.jetbrains.kotlin.utils.DFS
//
//class FirSupertypeLoopsTransformer : FirAbstractTreeTransformer() {
//    private lateinit var firSession: FirSession
//
//    override fun transformFile(file: FirFile, data: Nothing?): CompositeTransformResult<FirFile> {
//        // TODO: session should be injected into constructor
//        firSession = file.session
//        return super.transformFile(file, data)
//    }
//
//    private val neighbors: DFS.Neighbors<FirClassLikeDeclaration> = DFS.Neighbors { classLike ->
//        classLike.supertypeGraphNeighbours().mapNotNull { superType -> superType.lookupTag.toSymbol(firSession)?.toFirClassLike() }
//    }
//
//    override fun transformRegularClass(regularClass: FirRegularClass, data: Nothing?): CompositeTransformResult<FirDeclaration> {
//        val validSupertypes = mutableListOf<FirTypeRef>()
//
//        for (superTypeRef in regularClass.superTypeRefs) {
//            val coneSupertype = superTypeRef.coneTypeSafe<ConeClassLikeType>() ?: continue
//            val supertypeClassLike = coneSupertype.lookupTag.toSymbol(firSession)?.toFirClassLike() ?: continue
//
//            if (isReachable(supertypeClassLike, regularClass)) {
//                validSupertypes.add(
//                    FirErrorTypeRefImpl(firSession, superTypeRef.psi, "Recursion detected: ${superTypeRef.render()}")
//                )
//            }
//            else {
//                validSupertypes.add(superTypeRef)
//            }
//        }
//
//        val resultClass = regularClass.replaceSupertypes(validSupertypes)
//
//        return super.transformRegularClass(resultClass, data)
//    }
//
//    override fun transformTypeAlias(typeAlias: FirTypeAlias, data: Nothing?): CompositeTransformResult<FirDeclaration> {
//        if (isCyclicTypeAliasExpansion(typeAlias)) {
//            return typeAlias.replaceExpandTypeRef(
//                FirErrorTypeRefImpl(
//                    firSession,
//                    typeAlias.expandedTypeRef.psi,
//                    "Recursion detected: ${typeAlias.expandedTypeRef.render()}"
//                )
//            ).compose()
//        }
//
//        return typeAlias.compose()
//    }
//
//    private fun isCyclicTypeAliasExpansion(typeAlias: FirTypeAlias): Boolean {
//        val coneType = typeAlias.expandedConeType ?: return false
//        val classLike = coneType.lookupTag.toSymbol(firSession)?.toFirClassLike() ?: return false
//
//        return isReachable(classLike, typeAlias)
//    }
//
//    private fun isReachable(from: FirClassLikeDeclaration, to: FirClassLikeDeclaration): Boolean {
//        var result = false
//        DFS.dfs(listOf(from), neighbors, DFS.VisitedWithSet(), object : DFS.AbstractNodeHandler<FirClassLikeDeclaration, Unit>() {
//            override fun beforeChildren(current: FirClassLikeDeclaration): Boolean {
//                if (current == to) {
//                    result = true
//                    return false
//                }
//                return true
//            }
//
//            override fun result() = Unit
//        })
//
//        return result
//    }
//}
//
