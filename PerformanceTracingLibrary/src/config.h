/**
 * @file config.h
 *
 * @date 06.07.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef CONFIG_H_
#define CONFIG_H_


/* Traced as FLOAT */
#ifndef MEM_UNIT
# define MEM_UNIT "MB"
#endif
#ifndef MEM_MULT
# define MEM_MULT G_GUINT64_CONSTANT(1000000)
#endif


#endif /* CONFIG_H_ */
