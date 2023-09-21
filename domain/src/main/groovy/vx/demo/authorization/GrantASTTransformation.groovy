package vx.demo.authorization

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import groovy.transform.CompileStatic
import io.vertx.ext.web.RoutingContext
import vx.demo.web.Controller

/**
 * 
 * 
 * @author Konstantin Smirnov
 *
 */
@GroovyASTTransformation( phase = CANONICALIZATION )
@CompileStatic
class GrantASTTransformation implements ASTTransformation {

  private Set<String> uniques = new HashSet<>()
  
  @Override
  void visit( ASTNode[] nodes, SourceUnit source ) {
    if( !nodes ) return
    
    ClassNode classNode = (ClassNode) nodes.find{ it in ClassNode }
    MethodNode methodNode = (MethodNode) nodes.find{ ( it in MethodNode ) && matchesMethod( (MethodNode)it ) }
    
    if( classNode ) 
      augmentClass classNode
    else if( methodNode ){
      if( augmentMethod( methodNode ) )
        println "augmented ${methodNode.declaringClass.nameWithoutPackage}.${methodNode.name}(rc)"
    }
  }

  private void augmentClass( ClassNode classNode ) {
    List<AnnotationNode> annos = classNode.getAnnotations new ClassNode( Grant )
    if( !annos ) return
    
    Expression rolesValue = annos.first().getMember 'value'

    List<MethodNode> methods = classNode.methods.findAll this.&matchesMethod
    Number count = methods.count this.&augmentMethod.rcurry( rolesValue )
    
    classNode.superClass.methods.each{ 
      if( methods*.name.contains( it.name ) || !matchesMethod( it ) ) return
      MethodNode overridden = overrideMethod it
      classNode.addMethod overridden
      if( augmentMethod( overridden, rolesValue ) ) count++
    }
    
    println "augmented $classNode.nameWithoutPackage with ${count} methods"
  }
  
  private MethodNode overrideMethod( MethodNode source ) {
    (MethodNode)new AstBuilder().buildFromSpec{
      method( source.name, MethodNode.ACC_PUBLIC, Void.TYPE ){
        parameters{ parameter 'rc':RoutingContext }
        exceptions{}
        block{
          expression{
            methodCall{
              variable 'super'
              constant source.name
              argumentList{
                variable 'rc'
              }
            }
          }
        }
      }   
    }.first()
  }

  private boolean augmentMethod( MethodNode methodNode, Expression rolesValue = null ) {
    if( !uniques.add( methodNode.declaringClass.name + '#' + methodNode.name ) ) return false
    
    if( methodNode.code !in BlockStatement ) return false
    
    List<AnnotationNode> annos = methodNode.getAnnotations new ClassNode( Grant )
    if( !rolesValue && !annos || annos && annos.first().getMember( 'skip' ) ) return false
    if( annos ) rolesValue = annos.first().getMember 'value'

    Expression args = args varX( methodNode.parameters[ 0 ] ), rolesValue
    StaticMethodCallExpression check = callX new ClassNode( GrantUtil ), 'checkAuthorization', args
    BooleanExpression checkExpression = notX boolX( check )
    IfStatement ifStmt = ifS checkExpression, returnS( nullX() )
    
    ((BlockStatement)methodNode.code).statements.add 0, ifStmt
    
    true
  }
  
  private boolean matchesMethod( MethodNode mn ) {
    !mn.name.contains( '$' ) && mn.name !in Controller.methods*.name && mn.isPublic() && !mn.isStatic() && mn.returnType == new ClassNode( void ) && 1 == mn.parameters?.size() && mn.parameters[ 0 ].type == new ClassNode( RoutingContext )
  }
}
