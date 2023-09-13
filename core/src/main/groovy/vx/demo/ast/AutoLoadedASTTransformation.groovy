package vx.demo.ast

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import grails.gorm.annotation.Entity
import groovy.transform.CompileStatic

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
class AutoLoadedASTTransformation extends AbstractASTTransformation {

  static final List<Expression> fields = []
  
  static ClassNode bootstrap
  
  @Override
  void visit(ASTNode[] nodes, SourceUnit source) {
    if( 2 == nodes.size() && nodes[ 0 ] in AnnotationNode && AutoGORMInitializer.name == ((AnnotationNode)nodes[ 0 ]).classNode.name ){
      bootstrap = (ClassNode)nodes[ 1 ]
      ClassNode listType = makeClassSafeWithGenerics List, make( String )
      bootstrap.addField 'domainClasses', ACC_PRIVATE | ACC_FINAL, listType, listX( fields )
      
    }else {
      ClassNode targetClass = source.AST.classes[ 0 ]
      if( targetClass.getAnnotations( make( Entity ) ) ) fields << constX( targetClass.name )
    }
  }
}
