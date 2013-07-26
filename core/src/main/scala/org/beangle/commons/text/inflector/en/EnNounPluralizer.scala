/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.text.inflector.en

import org.beangle.commons.text.inflector.rule.AbstractRegexReplacementRule.disjunction
import java.util.Arrays
import java.util.Locale
import java.util.regex.Matcher
import org.beangle.commons.text.inflector.Rule
import org.beangle.commons.text.inflector.RuleBasedPluralizer
import org.beangle.commons.text.inflector.rule.AbstractRegexReplacementRule
import org.beangle.commons.text.inflector.rule.CategoryInflectionRule
import org.beangle.commons.text.inflector.rule.IrregularMappingRule
import org.beangle.commons.text.inflector.rule.RegexReplacementRule
import org.beangle.commons.text.inflector.rule.SuffixInflectionRule
import EnNounPluralizer._

object EnNounPluralizer {

  private val POSTFIX_ADJECTIVE_REGEX = "(" +
    "(?!major|lieutenant|brigadier|adjutant)\\S+(?=(?:-|\\s+)general)|" +
    "court(?=(?:-|\\s+)martial)" +
    ")(.*)"

  private val PREPOSITIONS = Array("about", "above", "across", "after", "among", "around", "at", "athwart", "before", "behind", "below", "beneath", "beside", "besides", "between", "betwixt", "beyond", "but", "by", "during", "except", "for", "from", "in", "into", "near", "of", "off", "on", "onto", "out", "over", "since", "till", "to", "under", "until", "unto", "upon", "with")

  private val NOMINATIVE_PRONOUNS = Map(("i" -> "we"), ("myself" -> "ourselves"), ("you" -> "you"), ("yourself" -> "yourselves"), ("she" -> "they"), ("herself" -> "themselves"), ("he" -> "they"), ("himself" -> "themselves"), ("it" -> "they"), ("itself" -> "themselves"), ("they" -> "they"), ("themself" -> "themselves"), ("mine" -> "ours"), ("yours" -> "yours"), ("hers" -> "theirs"), ("his" -> "theirs"), ("its" -> "theirs"), ("theirs" -> "theirs"))

  private val ACCUSATIVE_PRONOUNS = Map(("me" -> "us"), ("myself" -> "ourselves"), ("you" -> "you"), ("yourself" -> "yourselves"), ("her" -> "them"), ("herself" -> "themselves"), ("him" -> "them"), ("himself" -> "themselves"), ("it" -> "them"), ("itself" -> "themselves"), ("them" -> "them"), ("themself" -> "themselves"))

  private val IRREGULAR_NOUNS = Map(("child" -> "children"), ("brother" -> "brothers"), ("loaf" -> "loaves"), ("hoof" -> "hoofs"), ("beef" -> "beefs"), ("money" -> "monies"), ("mongoose" -> "mongooses"), ("ox" -> "oxen"), ("cow" -> "cows"), ("soliloquy" -> "soliloquies"), ("graffito" -> "graffiti"), ("prima donna" -> "prima donnas"), ("octopus" -> "octopuses"), ("genie" -> "genies"), ("ganglion" -> "ganglions"), ("trilby" -> "trilbys"), ("turf" -> "turfs"), ("numen" -> "numina"), ("atman" -> "atmas"), ("occiput" -> "occiputs"), ("corpus" -> "corpuses"), ("opus" -> "opuses"), ("genus" -> "genera"), ("mythos" -> "mythoi"), ("penis" -> "penises"), ("testis" -> "testes"), ("atlas" -> "atlases"))

  private val CATEGORY_UNINFLECTED_NOUNS = Array(".*fish", "tuna", "salmon", "mackerel", "trout", "bream", "sea[- ]bass", "carp", "cod", "flounder", "whiting", ".*deer", ".*sheep", "Portuguese", "Amoyese", "Borghese", "Congoese", "Faroese", "Foochowese", "Genevese", "Genoese", "Gilbertese", "Hottentotese", "Kiplingese", "Kongoese", "Lucchese", "Maltese", "Nankingese", "Niasese", "Pekingese", "Piedmontese", "Pistoiese", "Sarawakese", "Shavese", "Vermontese", "Wenchowese", "Yengeese", ".*[nrlm]ese", ".*pox", "graffiti", "djinn", "breeches", "britches", "clippers", "gallows", "hijinks", "headquarters", "pliers", "scissors", "testes", "herpes", "pincers", "shears", "proceedings", "trousers", "cantus", "coitus", "nexus", "contretemps", "corps", "debris", ".*ois", "siemens", ".*measles", "mumps", "diabetes", "jackanapes", "series", "species", "rabies", "chassis", "innings", "news", "mews")

  private val CATEGORY_MAN_MANS_RULE = Array("human", "Alabaman", "Bahaman", "Burman", "German", "Hiroshiman", "Liman", "Nakayaman", "Oklahoman", "Panaman", "Selman", "Sonaman", "Tacoman", "Yakiman", "Yokohaman", "Yuman")

  private val CATEGORY_EX_ICES_RULE = Array("codex", "murex", "silex")

  private val CATEGORY_IX_ICES_RULE = Array("radix", "helix")

  private val CATEGORY_UM_A_RULE = Array("bacterium", "agendum", "desideratum", "erratum", "stratum", "datum", "ovum", "extremum", "candelabrum")

  private val CATEGORY_US_I_RULE = Array("alumnus", "alveolus", "bacillus", "bronchus", "locus", "nucleus", "stimulus", "meniscus")

  private val CATEGORY_ON_A_RULE = Array("criterion", "perihelion", "aphelion", "phenomenon", "prolegomenon", "noumenon", "organon", "asyndeton", "hyperbaton")

  private val CATEGORY_A_AE_RULE = Array("alumna", "alga", "vertebra", "persona")

  private val CATEGORY_O_OS_RULE = Array("albino", "archipelago", "armadillo", "commando", "crescendo", "fiasco", "ditto", "dynamo", "embryo", "ghetto", "guano", "inferno", "jumbo", "lumbago", "magneto", "manifesto", "medico", "octavo", "photo", "pro", "quarto", "canto", "lingo", "generalissimo", "stylo", "rhino", "casino", "auto", "macro", "zero", "solo", "soprano", "basso", "alto", "contralto", "tempo", "piano", "virtuoso")

  private val CATEGORY_SINGULAR_S_RULE = Array(".*ss", "acropolis", "aegis", "alias", "asbestos", "bathos", "bias", "bronchitis", "bursitis", "caddis", "cannabis", "canvas", "chaos", "cosmos", "dais", "digitalis", "epidermis", "ethos", "eyas", "gas", "glottis", "hubris", "ibis", "lens", "mantis", "marquis", "metropolis", "pathos", "pelvis", "polis", "rhinoceros", "sassafras", "trellis", ".*us", "[A-Z].*es", "ephemeris", "iris", "clitoris", "chrysalis", "epididymis", ".*itis")
}

/**
 * <p>
 * EnNounPluralizer class.
 * </p>
 *
 * @author chaostone
 */
class EnNounPluralizer extends RuleBasedPluralizer {

  private val enrules = List(new RegexReplacementRule("^(\\s)$", "$1"), new CategoryInflectionRule(CATEGORY_UNINFLECTED_NOUNS,
    "-", "-"), new AbstractRegexReplacementRule("(?i)^(?:" + POSTFIX_ADJECTIVE_REGEX + ")$") {

    override def replace(m: Matcher): String = {
      EnNounPluralizer.this.pluralize(m.group(1)) + m.group(2)
    }
  }, new AbstractRegexReplacementRule("(?i)(.*?)((?:-|\\s+)(?:" + disjunction(PREPOSITIONS) +
    "|d[eu])(?:-|\\s+))a(?:-|\\s+)(.*)") {

    override def replace(m: Matcher): String = {
      EnNounPluralizer.this.pluralize(m.group(1)) + m.group(2) +
        EnNounPluralizer.this.pluralize(m.group(3))
    }
  }, new AbstractRegexReplacementRule("(?i)(.*?)((-|\\s+)(" + disjunction(PREPOSITIONS) + "|d[eu])((-|\\s+)(.*))?)") {

    override def replace(m: Matcher): String = {
      EnNounPluralizer.this.pluralize(m.group(1)) + m.group(2)
    }
  }, new IrregularMappingRule(NOMINATIVE_PRONOUNS, "(?i)" + disjunction(NOMINATIVE_PRONOUNS.keySet)), new IrregularMappingRule(ACCUSATIVE_PRONOUNS,
    "(?i)" + disjunction(ACCUSATIVE_PRONOUNS.keySet)), new IrregularMappingRule(ACCUSATIVE_PRONOUNS,
    "(?i)(" + disjunction(PREPOSITIONS) + "\\s)" + "(" + disjunction(ACCUSATIVE_PRONOUNS.keySet) +
      ")") {

    override def replace(m: Matcher): String = {
      m.group(1) + mappings.get(m.group(2).toLowerCase())
    }
  }, new IrregularMappingRule(IRREGULAR_NOUNS, "(?i)(.*)\\b" + disjunction(IRREGULAR_NOUNS.keySet) +
    "$"), new CategoryInflectionRule(CATEGORY_MAN_MANS_RULE, "-man", "-mans"), new RegexReplacementRule("(?i)(\\S*)(person)$",
    "$1people"), new SuffixInflectionRule("-man", "-man", "-men"), new SuffixInflectionRule("-[lm]ouse",
    "-ouse", "-ice"), new SuffixInflectionRule("-tooth", "-tooth", "-teeth"), new SuffixInflectionRule("-goose",
    "-goose", "-geese"), new SuffixInflectionRule("-foot", "-foot", "-feet"), new SuffixInflectionRule("-ceps",
    "-", "-"), new SuffixInflectionRule("-zoon", "-zoon", "-zoa"), new SuffixInflectionRule("-[csx]is",
    "-is", "-es"), new CategoryInflectionRule(CATEGORY_EX_ICES_RULE, "-ex", "-ices"), new CategoryInflectionRule(CATEGORY_IX_ICES_RULE,
    "-ix", "-ices"), new CategoryInflectionRule(CATEGORY_UM_A_RULE, "-um", "-a"), new CategoryInflectionRule(CATEGORY_US_I_RULE,
    "-us", "-i"), new CategoryInflectionRule(CATEGORY_ON_A_RULE, "-on", "-a"), new CategoryInflectionRule(CATEGORY_A_AE_RULE,
    "-a", "-ae"), new CategoryInflectionRule(CATEGORY_SINGULAR_S_RULE, "-s", "-ses"), new RegexReplacementRule("^([A-Z].*s)$",
    "$1es"), new SuffixInflectionRule("-[cs]h", "-h", "-hes"), new SuffixInflectionRule("-x", "-x", "-xes"), new SuffixInflectionRule("-z",
    "-z", "-zes"), new SuffixInflectionRule("-[aeo]lf", "-f", "-ves"), new SuffixInflectionRule("-[^d]eaf",
    "-f", "-ves"), new SuffixInflectionRule("-arf", "-f", "-ves"), new SuffixInflectionRule("-[nlw]ife",
    "-fe", "-ves"), new SuffixInflectionRule("-[aeiou]y", "-y", "-ys"), new RegexReplacementRule("^([A-Z].*y)$",
    "$1s"), new SuffixInflectionRule("-y", "-y", "-ies"), new CategoryInflectionRule(CATEGORY_O_OS_RULE,
    "-o", "-os"), new SuffixInflectionRule("-[aeiou]o", "-o", "-os"), new SuffixInflectionRule("-o",
    "-o", "-oes"), new SuffixInflectionRule("-", "-s"))

  rules = enrules

  locale = Locale.ENGLISH

  protected override def postProcess(trimmedWord: String, pluralizedWord: String): String = {
    if (trimmedWord == "I") {
      return pluralizedWord
    }
    super.postProcess(trimmedWord, pluralizedWord)
  }
}
